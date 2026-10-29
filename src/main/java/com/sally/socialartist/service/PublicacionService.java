package com.sally.socialartist.service;

import com.sally.socialartist.dto.request.PublicacionRequestDTO;
import com.sally.socialartist.dto.response.PublicacionResponseDTO;
import com.sally.socialartist.model.Publicacion;
import com.sally.socialartist.model.Usuario;
import com.sally.socialartist.repository.PublicacionRepository;
import com.sally.socialartist.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoService almacenamientoService;
    private final BitacoraAdminService bitacoraAdminService;

    /**
     * Explorar: publicaciones agrupadas por usuario.
     * Se ordena por fecha desc y se agrupa conservando el orden de aparicion:
     * el usuario con la publicacion mas reciente va primero y todas las suyas quedan juntas.
     */
    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> listarExplorar() {
        return publicacionRepository.findAllByOrderByFechaPublicacionDescIdDesc().stream()
                .collect(Collectors.groupingBy(p -> p.getUsuario().getId(), LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .flatMap(List::stream)
                .map(this::aDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicacionResponseDTO obtener(Long id) {
        return publicacionRepository.findById(id)
                .map(this::aDTO)
                .orElseThrow(this::noEncontrada);
    }

    @Transactional
    public PublicacionResponseDTO crear(PublicacionRequestDTO dto, Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));

        String archivo = almacenamientoService.guardar(dto.getImagen());

        Publicacion publicacion = new Publicacion();
        publicacion.setTitulo(dto.getTitulo().trim());
        String descripcion = dto.getDescripcion() == null ? "" : dto.getDescripcion().trim();
        publicacion.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        publicacion.setRutaImagen(archivo);
        publicacion.setUsuario(usuario);
        return aDTO(publicacionRepository.save(publicacion));
    }

    @Transactional
    public void eliminar(Long idPublicacion, Long idAdmin) {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(this::noEncontrada);
        Usuario admin = usuarioRepository.getReferenceById(idAdmin);

        bitacoraAdminService.registrar(admin, "ELIMINAR_PUBLICACION",
                "Publicación #" + publicacion.getId() + " '" + publicacion.getTitulo()
                        + "' de @" + publicacion.getUsuario().getNombreUsuario());

        publicacionRepository.delete(publicacion);
        almacenamientoService.eliminar(publicacion.getRutaImagen());
    }

    private ResponseStatusException noEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "La publicación no existe");
    }

    private PublicacionResponseDTO aDTO(Publicacion p) {
        return new PublicacionResponseDTO(
                p.getId(),
                p.getTitulo(),
                p.getDescripcion(),
                "/uploads/" + p.getRutaImagen(),
                p.getUsuario().getId(),
                p.getUsuario().getNombreUsuario(),
                p.getFechaPublicacion());
    }
}

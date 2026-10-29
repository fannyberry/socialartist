package com.sally.socialartist.controller;

import com.sally.socialartist.dto.request.PublicacionRequestDTO;
import com.sally.socialartist.dto.response.PublicacionResponseDTO;
import com.sally.socialartist.dto.response.UsuarioResponseDTO;
import com.sally.socialartist.service.PublicacionService;
import com.sally.socialartist.service.SesionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;
    private final SesionService sesionService;

    @GetMapping
    public List<PublicacionResponseDTO> explorar(HttpSession sesion) {
        sesionService.requerirUsuario(sesion);
        return publicacionService.listarExplorar();
    }

    @GetMapping("/{id}")
    public PublicacionResponseDTO obtener(@PathVariable Long id, HttpSession sesion) {
        sesionService.requerirUsuario(sesion);
        return publicacionService.obtener(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PublicacionResponseDTO publicar(@Valid @ModelAttribute PublicacionRequestDTO dto, HttpSession sesion) {
        UsuarioResponseDTO usuario = sesionService.requerirUsuarioComun(sesion);
        return publicacionService.crear(dto, usuario.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, HttpSession sesion) {
        UsuarioResponseDTO admin = sesionService.requerirAdmin(sesion);
        publicacionService.eliminar(id, admin.getId());
    }
}

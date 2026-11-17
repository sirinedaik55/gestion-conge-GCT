package tn.gct.conge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.gct.conge.dto.TypeCongeDto;
import tn.gct.conge.repository.TypeCongeRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/types-conge")
@RequiredArgsConstructor
@Tag(name = "Types de Congé", description = "Consultation des types de congé et de leurs règles (maternité, annuel, maladie...)")
public class TypeCongeController {

    private final TypeCongeRepository typeCongeRepository;

    @GetMapping
    @Operation(summary = "Lister tous les types de congé paramétrés")
    public ResponseEntity<List<TypeCongeDto>> getAllTypes() {
        List<TypeCongeDto> list = typeCongeRepository.findAll().stream()
                .map(t -> TypeCongeDto.builder()
                        .id(t.getId())
                        .nom(t.getNom())
                        .code(t.getCode())
                        .description(t.getDescription())
                        .joursMaxParAn(t.getJoursMaxParAn())
                        .autoApprouve(t.getAutoApprouve())
                        .justificatifRequis(t.getJustificatifRequis())
                        .cumulable(t.getCumulable())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}

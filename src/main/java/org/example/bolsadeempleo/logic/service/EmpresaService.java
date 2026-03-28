package org.example.bolsadeempleo.logic.service;

import lombok.Getter;
import org.example.bolsadeempleo.logic.*;
import org.example.bolsadeempleo.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PuestoRepository puestoRepository;

    @Autowired
    private OferenteRepository oferenteRepository;

    @Autowired
    private HabilidadRepository habilidadRepository;

    @Autowired
    private RequisitoPuestoRepository requisitoPuestoRepository;

    @Autowired
    private CaracteristicaRepository caracteristicaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

}
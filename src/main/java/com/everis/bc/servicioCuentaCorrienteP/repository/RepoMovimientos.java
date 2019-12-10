package com.everis.bc.servicioCuentaCorrienteP.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.everis.bc.servicioCuentaCorrienteP.model.Movimientos;

public interface RepoMovimientos extends ReactiveMongoRepository<Movimientos, String>{

}

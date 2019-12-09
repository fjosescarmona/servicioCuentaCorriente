package com.everis.bc.servicioCuentaCorriente.repo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.everis.bc.servicioCuentaCorriente.model.Movimientos;

public interface RepoMovimientos extends ReactiveMongoRepository<Movimientos, String>{

}

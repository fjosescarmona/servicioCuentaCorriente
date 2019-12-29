package com.everis.bc.servicioCuentaCorrienteP.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.everis.bc.servicioCuentaCorrienteP.model.Deudores;
import com.everis.bc.servicioCuentaCorrienteP.model.CuentaCorrienteP;

import reactor.core.publisher.Flux;


public interface RepoD extends ReactiveMongoRepository<Deudores, String>{
	@Query("{ 'documento': {$in:[ ?0 ]} }")
	public Flux<CuentaCorrienteP> findByTitularesDocList(List<String> docs);
}

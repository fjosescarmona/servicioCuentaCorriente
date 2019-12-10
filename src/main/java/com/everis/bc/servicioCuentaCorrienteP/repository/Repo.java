package com.everis.bc.servicioCuentaCorrienteP.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.everis.bc.servicioCuentaCorrienteP.model.CuentaCorrienteP;

import reactor.core.publisher.Mono;

public interface Repo extends ReactiveMongoRepository<CuentaCorrienteP, String>{
	@Query("{ 'titulares.doc': ?0 }")
	public Mono<CuentaCorrienteP> findByTitularesDoc(String doc);
	@Query("{ 'nro_cuenta': ?0 }")
	public Mono<CuentaCorrienteP> findByNro_cuenta(String nro_cuenta);
	

	
	//public boolean existByTitulares(String doc);
}

package com.everis.bc.servicioCuentaCorriente.repo;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.everis.bc.servicioCuentaCorriente.model.CuentaCorriente;
import reactor.core.publisher.Mono;

public interface Repo extends ReactiveMongoRepository<CuentaCorriente, String>{
	@Query("{ 'titulares.doc': ?0 }")
	public Mono<CuentaCorriente> findByTitularesDoc(String doc);
	@Query("{ 'nro_cuenta': ?0 }")
	public Mono<CuentaCorriente> findByNro_cuenta(String nro_cuenta);
	
	//public boolean existByTitulares(String doc);
}

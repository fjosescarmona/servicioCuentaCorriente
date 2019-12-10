package com.everis.bc.servicioCuentaCorriente.service;

import com.everis.bc.servicioCuentaCorriente.model.Persona;

import reactor.core.publisher.Mono;

public interface ServiceClient {
	public Mono<Persona> saveData(Persona persona);
}

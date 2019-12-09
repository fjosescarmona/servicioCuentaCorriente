package com.everis.bc.servicioCuentaCorriente.service;


import java.util.Map;

import com.everis.bc.servicioCuentaCorriente.model.CuentaCorriente;
import com.everis.bc.servicioCuentaCorriente.model.Movimientos;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceCta {
	
	public Mono<Map<String, Object>> saveData(CuentaCorriente cuenta);
	
	public Flux<CuentaCorriente> getData();
	
	public Mono<CuentaCorriente> getDataByDoc(String doc);

	public Mono<Map<String, Object>> getSaldo(String nro_cuenta);

	public Mono<Void> deleteData(String id);
	
	public Mono<CuentaCorriente> editData(String id, CuentaCorriente cuenta);
	
	public Mono<Map<String, Object>> saveMovimiento(Movimientos mov);
	
	public Flux<Movimientos> getMovimientos();
}

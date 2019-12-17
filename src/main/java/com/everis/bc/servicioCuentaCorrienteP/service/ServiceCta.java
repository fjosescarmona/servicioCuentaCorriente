package com.everis.bc.servicioCuentaCorrienteP.service;


import java.util.Map;

import com.everis.bc.servicioCuentaCorrienteP.model.CuentaCorrienteP;
import com.everis.bc.servicioCuentaCorrienteP.model.Movimientos;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceCta {
	
	public Mono<CuentaCorrienteP> saveData(CuentaCorrienteP cuenta);
	
	public Flux<CuentaCorrienteP> getData();
	
	public Mono<CuentaCorrienteP> getDataByDoc(String doc);

	public Mono<Map<String, Object>> getSaldo(String nro_cuenta);

	public Mono<Void> deleteData(String id);
	
	public Mono<CuentaCorrienteP> editData(String id, CuentaCorrienteP cuenta);
	
	public Mono<Map<String, Object>> saveMovimiento(Movimientos mov);
	
	public Flux<Movimientos> getMovimientos(String nro_cuenta);
}

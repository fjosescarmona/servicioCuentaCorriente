package com.everis.bc.servicioCuentaCorrienteP.service;


import java.util.List;
import java.util.Map;

import com.everis.bc.servicioCuentaCorrienteP.model.CuentaCorrienteP;
import com.everis.bc.servicioCuentaCorrienteP.model.Deudores;
import com.everis.bc.servicioCuentaCorrienteP.model.Movimientos;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceCta {
	
	public Mono<CuentaCorrienteP> saveData(CuentaCorrienteP cuenta);
	
	public Flux<CuentaCorrienteP> getData();
	
	public Flux<CuentaCorrienteP> getDataByDoc(String doc);

	public Mono<Map<String, Object>> getSaldo(String nro_cuenta);

	public Mono<Void> deleteData(String id);
	
	public Mono<CuentaCorrienteP> editData(String id, CuentaCorrienteP cuenta);
	
	public Mono<Movimientos> savePagotdc(Movimientos mov);
	
	public Mono<Movimientos> saveDeposito(Movimientos mov);
	
	public Mono<Movimientos> saveRetiro(Movimientos mov);
	
	public Mono<Movimientos> getTransfer(Movimientos mov);
	
	public Mono<Movimientos> setTransfer(Movimientos mov);
	
	public Flux<Movimientos> getMovimientos(String nro_cuenta);
	
	public Flux<Movimientos> getRangeMovimientos(String nro_cuenta, String from, String to);
	
	public Flux<Deudores> saveDeudoresPcorriente(List<Deudores> deudores);
}

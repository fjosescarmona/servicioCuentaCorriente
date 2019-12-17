package com.everis.bc.servicioCuentaCorrienteP.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.everis.bc.servicioCuentaCorrienteP.model.CuentaCorrienteP;
import com.everis.bc.servicioCuentaCorrienteP.model.Movimientos;
import com.everis.bc.servicioCuentaCorrienteP.service.ServiceCta;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CuentaCorrientePController {

	@Autowired
	private ServiceCta s_cuenta;

	@PostMapping("/saveCcorrientePData")
	public Mono<CuentaCorrienteP> saveCcorrienteData(@RequestBody CuentaCorrienteP cuenta){
		return s_cuenta.saveData(cuenta);
	}
	
	@GetMapping("/getCcorrientePData/{doc}")
	public Mono<CuentaCorrienteP> getCcorrienteData(@PathVariable("doc") String doc){
		return s_cuenta.getDataByDoc(doc);
	}
	
	@GetMapping("/getCcorrientePSaldo/{nro_cuenta}")
	public Mono<Map<String, Object>> getCcorrienteSaldo(@PathVariable("nro_cuenta") String nro_cuenta){
		return s_cuenta.getSaldo(nro_cuenta);
	}
	
	@PostMapping("/saveMovimientosCorrienteP")
	public Mono<Map<String, Object>> saveMovimientosCorriente(@RequestBody Movimientos movimiento){
		return s_cuenta.saveMovimiento(movimiento);
	}
	
	@GetMapping("/getMovimientosCorrienteP/{nro_cuenta}")
	public Flux<Movimientos> getMovimientosCorriente(@PathVariable("nro_cuenta") String nro_cuenta){
		return s_cuenta.getMovimientos(nro_cuenta);
	}

}

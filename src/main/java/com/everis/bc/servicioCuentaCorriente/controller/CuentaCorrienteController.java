package com.everis.bc.servicioCuentaCorriente.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.everis.bc.servicioCuentaCorriente.model.CuentaCorriente;
import com.everis.bc.servicioCuentaCorriente.model.Movimientos;
import com.everis.bc.servicioCuentaCorriente.service.ServiceCta;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CuentaCorrienteController {

	@Autowired
	private ServiceCta s_cuenta;

	@PostMapping("/saveCcorrienteData")
	public Mono<Map<String, Object>> saveCcorrienteData(@RequestBody CuentaCorriente cuenta){
		return s_cuenta.saveData(cuenta);
	}
	
	@GetMapping("/getCcorrienteData/{doc}")
	public Mono<CuentaCorriente> getCcorrienteData(@PathVariable("doc") String doc){
		return s_cuenta.getDataByDoc(doc);
	}
	
	@PostMapping("/saveMovimientosCorriente")
	public Mono<Map<String, Object>> saveMovimientosCorriente(@RequestBody Movimientos movimiento){
		return s_cuenta.saveMovimiento(movimiento);
	}
	
	@GetMapping("/getMovimientosCorriente")
	public Flux<Movimientos> getMovimientosCorriente(){
		return s_cuenta.getMovimientos();
	}

}

package com.everis.bc.servicioCuentaCorriente.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.everis.bc.servicioCuentaCorriente.model.CuentaCorriente;
import com.everis.bc.servicioCuentaCorriente.model.Listas;
import com.everis.bc.servicioCuentaCorriente.model.Movimientos;
import com.everis.bc.servicioCuentaCorriente.repo.Repo;
import com.everis.bc.servicioCuentaCorriente.repo.RepoMovimientos;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ServiceCtaImplement implements ServiceCta {

	@Autowired
	private Repo repo1;
	@Autowired
	private RepoMovimientos repoMov;
	//private WebClient client;
	
	//private List<String> aux;
	@Override
	public Mono<Map<String, Object>> saveData(CuentaCorriente cuenta) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		//validando tipo de cuenta: Persona o Empresa
		if(cuenta.getTipo().equals("P")) {
			String aux="";
			
			
		
			cuenta.getTitulares().stream().forEach(titular->{
				 Mono<CuentaCorriente> cta=repo1.findByTitularesDoc(titular.getDoc());
				
				if(cta.hasElement() != null) {
					
					//System.out.println(repo1.findByTitularesDoc(titular.getDoc()));
					aux.concat(titular.getDoc());
					//System.out.println(aux);
				}
			});
			if(aux.equals("")) {
				//Map<String, Object> persona = new HashMap<String, Object>();
				//persona.put("nombre", cuenta.getTitulares());
				//client.get().uri("/saveEmpresaData", persona);
				return repo1.save(cuenta).map(cta->{
					respuesta.put("Mensaje: ", "guardado correcto");
					return  respuesta;
				});
			}else {
				respuesta.put("Error: ", "El titular "+aux+" ya posee una cuenta corriente");
				return Mono.just(respuesta);
			}
			
		}else {
			//Map<String, Object> empresa = new HashMap<String, Object>();
			//empresa.put("", cuenta.getTitulares());
			return repo1.save(cuenta).map(cta->{
				//client.get().uri("/saveEmpresaData", empresa);
				respuesta.put("Mensaje: ", "guardado correcto");
				return  respuesta;
			});
		}
		
		
		// TODO Auto-generated method stub
		
		/*return repo1.save(cuenta).map(cta->{
			respuesta.put("Mensaje: ", "guardado correcto");
			return  respuesta;
		});*/
		//respuesta.put("Mensaje: ", "guardado correcto");
		
	}

	@Override
	public Flux<CuentaCorriente> getData() {
		// TODO Auto-generated method stub
		return repo1.findAll();
	}

	@Override
	public Mono<Void> deleteData(String id) {
		// TODO Auto-generated method stub
		return repo1.findById(id).flatMap(cta->repo1.delete(cta));
	}

	@Override
	public Mono<CuentaCorriente> editData(String id, CuentaCorriente cuenta) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<Map<String, Object>> saveMovimiento(Movimientos movimiento) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return repo1.findByNro_cuenta(movimiento.getNro_cuenta()).map(cta -> {
			Double saldo=cta.getSaldo();
			switch(movimiento.getDescripcion()) {
				case "retiro":{
					if(saldo>=movimiento.getMonto()) {
						cta.setSaldo(saldo-movimiento.getMonto());
						repo1.save(cta).subscribe();
						repoMov.save(movimiento).subscribe();
						respuesta.put("Result", "Retiro realizado, su nuevo saldo es: "+(saldo-movimiento.getMonto()));
						return respuesta;
					}else {
						respuesta.put("Result", "Su saldo no es suficiente para realizar la operaciòn");
						return respuesta;
					}
					
				}
				case "deposito":{
					cta.setSaldo(saldo+movimiento.getMonto());
					repo1.save(cta).subscribe();
					repoMov.save(movimiento);
					respuesta.put("Result", "Deposito realizado, su nuevo saldo es: "+(saldo+movimiento.getMonto()));
					return respuesta;
				}
			}
			respuesta.put("Error", "Especifique el movimiento a realizar");
			return respuesta;
		});
		
		/*return repoMov.save(movimiento).map(mov->{
			respuesta.put("Mensaje: ", "ok");
			return  respuesta;
		});*/
	}

	@Override
	public Flux<Movimientos> getMovimientos() {
		// TODO Auto-generated method stub
		return repoMov.findAll();
	}

	@Override
	public Mono<CuentaCorriente> getDataByDoc(String doc) {
		// TODO Auto-generated method stub
		return repo1.findByTitularesDoc(doc);
	}

}

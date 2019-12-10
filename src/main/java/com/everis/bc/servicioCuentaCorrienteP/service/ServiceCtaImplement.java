package com.everis.bc.servicioCuentaCorrienteP.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.everis.bc.servicioCuentaCorrienteP.model.CuentaCorrienteP;
import com.everis.bc.servicioCuentaCorrienteP.model.Empresa;
import com.everis.bc.servicioCuentaCorrienteP.model.Listas;
import com.everis.bc.servicioCuentaCorrienteP.model.Movimientos;
import com.everis.bc.servicioCuentaCorrienteP.model.Persona;
import com.everis.bc.servicioCuentaCorrienteP.repository.Repo;
import com.everis.bc.servicioCuentaCorrienteP.repository.RepoMovimientos;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Configuration
public class ServiceCtaImplement implements ServiceCta {

	@Autowired
	private Repo repo1;
	@Autowired
	private RepoMovimientos repoMov;
	
	@Override
	public Mono<Map<String, Object>> saveData(CuentaCorrienteP cuenta) {
		Map<String, Object> respuesta = new HashMap<String, Object>();
		//validando tipo de cuenta: Persona o Empresa
		
			
			String aux="";
			
			
			cuenta.getTitulares().stream().forEach(titular->{
				//Mono<CuentaCorriente> cta=repo1.findByTitularesDoc(titular.getDoc()).subscribe();
				
				
				if(!repo1.findByTitularesDoc(titular.getDoc()).toString().equals("")){
					aux.concat(titular.getDoc());
				}
			/*client.post().uri("/savePersonaData").accept(MediaType.APPLICATION_JSON_UTF8)
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.body(BodyInserters.fromObject(titular))
					.retrieve()
					.bodyToMono(Persona.class).subscribe();*/
			
			});
			if(aux.equals("")) {
				return repo1.save(cuenta).map(cta->{
					respuesta.put("Mensaje: ", "guardado correcto");
					return  respuesta;
				});
			}else {
				return Mono.just(cuenta).map(c->{
					respuesta.put("Error", "Las cuentas los clientes "+aux+" ya poseen cuenta corriente.");
					return respuesta;
				});
			}
			
		
		
	}

	@Override
	public Flux<CuentaCorrienteP> getData() {
		// TODO Auto-generated method stub
		return repo1.findAll();
	}

	@Override
	public Mono<Void> deleteData(String id) {
		// TODO Auto-generated method stub
		return repo1.findById(id).flatMap(cta->repo1.delete(cta));
	}

	@Override
	public Mono<CuentaCorrienteP> editData(String id, CuentaCorrienteP cuenta) {
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
	public Mono<CuentaCorrienteP> getDataByDoc(String doc) {
		// TODO Auto-generated method stub
		return repo1.findByTitularesDoc(doc);
	}

	@Override
	public Mono<Map<String, Object>> getSaldo(String nro_cuenta) {
		// TODO Auto-generated method stub
		Map<String, Object> respuesta = new HashMap<String, Object>();
		
		return repo1.findByNro_cuenta(nro_cuenta).map(cta->{
			respuesta.put("saldo", cta.getSaldo());
			return respuesta;
		});
		//return null;
	}

}

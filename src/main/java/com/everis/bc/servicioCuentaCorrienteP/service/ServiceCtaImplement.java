package com.everis.bc.servicioCuentaCorrienteP.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.everis.bc.servicioCuentaCorrienteP.model.CuentaCorrienteP;
import com.everis.bc.servicioCuentaCorrienteP.model.Movimientos;
import com.everis.bc.servicioCuentaCorrienteP.model.Listas;
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
	@Autowired
	@Qualifier("tc")
	private WebClient client;
	@Autowired
	@Qualifier("ahorro")
	private WebClient ahorro;
	@Autowired
	@Qualifier("pcorriente")
	private WebClient pcorriente;
	@Autowired
	@Qualifier("ecorriente")
	private WebClient ecorriente;
	@Autowired
	@Qualifier("vip")
	private WebClient vip;

	private List<String> docs;

	@Override
	public Mono<CuentaCorrienteP> saveData(CuentaCorrienteP cuenta) {
		Map<String, Object> respuesta = new HashMap<String, Object>();

		List<String> doc = new ArrayList<>();
		for (Listas h : cuenta.getTitulares()) {
			doc.add(h.getDoc());
		}

		return repo1.findByTitularesDocList(doc).flatMap(ctas -> {
			return Mono.just(ctas);
		}).switchIfEmpty(repo1.save(cuenta).flatMap(cta -> {
			return Mono.just(cta);
		})).next();

		/*
		 * return repo1.findByTitularesDocList(docs).map(ctadb -> {
		 * respuesta.put("Error", "El cliente ya posee una cuenta corriente"); return
		 * respuesta; }).switchIfEmpty( return repo1.save(cuenta).map(cta->{
		 * respuesta.put("Mensaje: ", "guardado correcto"); return respuesta; }); );
		 * 
		 * });
		 */

	}

	@Override
	public Flux<CuentaCorrienteP> getData() {
		// TODO Auto-generated method stub
		return repo1.findAll();
	}

	@Override
	public Mono<Void> deleteData(String id) {
		// TODO Auto-generated method stub
		return repo1.findById(id).flatMap(cta -> repo1.delete(cta));
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
			Double saldo = cta.getSaldo();
			switch (movimiento.getDescripcion()) {
			case "retiro": {
				if (saldo >= movimiento.getMonto()) {
					cta.setSaldo(saldo - movimiento.getMonto());
					repo1.save(cta).subscribe();
					repoMov.save(movimiento).subscribe();
					respuesta.put("Result", "Retiro realizado, su nuevo saldo es: " + (saldo - movimiento.getMonto()));
					return respuesta;
				} else {
					respuesta.put("Result", "Su saldo no es suficiente para realizar la operaciòn");
					return respuesta;
				}

			}
			case "deposito": {
				cta.setSaldo(saldo + movimiento.getMonto());
				repo1.save(cta).subscribe();
				repoMov.save(movimiento).subscribe();
				respuesta.put("Result", "Deposito realizado, su nuevo saldo es: " + (saldo + movimiento.getMonto()));
				return respuesta;
			}
			}
			respuesta.put("Error", "Especifique el movimiento a realizar");
			return respuesta;
		});

		/*
		 * return repoMov.save(movimiento).map(mov->{ respuesta.put("Mensaje: ", "ok");
		 * return respuesta; });
		 */
	}

	@Override
	public Flux<Movimientos> getMovimientos(String nro_cuenta) {
		// TODO Auto-generated method stub
		return repoMov.findByNro_cuenta(nro_cuenta);
	}

	@Override
	public Mono<CuentaCorrienteP> getDataByDoc(String doc) {
		// TODO Auto-generated method stub
		return repo1.findByTitularesDoc(doc).switchIfEmpty(Mono.just("").flatMap(r -> {
			CuentaCorrienteP cta2 = new CuentaCorrienteP();
			return Mono.just(cta2);
		}));
	}

	@Override
	public Mono<Map<String, Object>> getSaldo(String nro_cuenta) {
		// TODO Auto-generated method stub
		Map<String, Object> respuesta = new HashMap<String, Object>();

		return repo1.findByNro_cuenta(nro_cuenta).map(cta -> {
			respuesta.put("saldo", cta.getSaldo());
			return respuesta;
		});
		// return null;
	}

	@Override
	public Mono<Movimientos> savePagotdc(Movimientos mov) {
		// TODO Auto-generated method stub
		return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {

			if (cta.getSaldo() >= mov.getMonto()) {
				cta.setSaldo(cta.getSaldo() - mov.getMonto());
				// DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
				// LocalDateTime now = LocalDateTime.now();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("nro_tarjeta", mov.getNro_tarjeta());
				params.put("descripcion", "pago");
				params.put("monto", mov.getMonto());
				params.put("fecha", mov.getFecha());

				return client.post().uri("/savePagoTC").accept(MediaType.APPLICATION_JSON_UTF8)
						.body(BodyInserters.fromObject(params)).retrieve().bodyToMono(Movimientos.class)
						.flatMap(ptdc -> {

							if (!ptdc.getNro_tarjeta().equals(null)) {
								return repo1.save(cta).flatMap(ncta -> {
									return repoMov.save(mov);
								});
							} else {
								return Mono.just(new Movimientos());
							}

						});

			} else {

				return Mono.just(new Movimientos());
			}
		});
	}

	@Override
	public Mono<Movimientos> saveDeposito(Movimientos mov) {
		// TODO Auto-generated method stub
		return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {

			cta.setSaldo(cta.getSaldo() + mov.getMonto());
			return repo1.save(cta).flatMap(ncta -> {
				return repoMov.save(mov);
			});
		});
	}

	@Override
	public Mono<Movimientos> saveRetiro(Movimientos mov) {
		// TODO Auto-generated method stub
		return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {
			if (cta.getSaldo() >= mov.getMonto()) {
				cta.setSaldo(cta.getSaldo() - mov.getMonto());
				return repo1.save(cta).flatMap(ncta -> {
					return repoMov.save(mov);
				});
			} else {
				return Mono.just(new Movimientos());
			}
		});
	}

	@Override
	public Mono<Movimientos> getTransfer(Movimientos mov) {
		// TODO Auto-generated method stub
		return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {

			cta.setSaldo(cta.getSaldo() + mov.getMonto());
			return repo1.save(cta).flatMap(ncta -> {
				return repoMov.save(mov);
			});
		});
	}

	@Override
	public Mono<Movimientos> setTransfer(Movimientos mov) {
		// TODO Auto-generated method stub
		return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {

			switch (mov.getCuentaToTipo()) {
			case "ahorro": {

				if (cta.getSaldo() >= mov.getMonto()) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("nro_cuenta", mov.getCuentaTo());
					params.put("cuentaFrom", mov.getNro_cuenta());
					params.put("descripcion", "transferencia");
					params.put("monto", mov.getMonto());
					params.put("fecha", mov.getFecha());

					return ahorro.post().uri("/getTransferAhorro").accept(MediaType.APPLICATION_JSON_UTF8)
							.body(BodyInserters.fromObject(params)).retrieve().bodyToMono(Movimientos.class)
							.flatMap(ptdc -> {

								if (!ptdc.getNro_cuenta().equals(null)) {
									cta.setSaldo(cta.getSaldo() - mov.getMonto());
									return repo1.save(cta).flatMap(ncta -> {
										return repoMov.save(mov);
									});
								} else {
									return Mono.just(new Movimientos());
								}

							});
				} else {
					return Mono.just(new Movimientos());
				}
			}
			case "pcorriente": {
				if (cta.getSaldo() >= mov.getMonto()) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("nro_cuenta", mov.getCuentaTo());
					params.put("cuentaFrom", mov.getNro_cuenta());
					params.put("descripcion", "transferencia");
					params.put("monto", mov.getMonto());
					params.put("fecha", mov.getFecha());

					return pcorriente.post().uri("/getTransferPcorriente").accept(MediaType.APPLICATION_JSON_UTF8)
							.body(BodyInserters.fromObject(params)).retrieve().bodyToMono(Movimientos.class)
							.flatMap(ptdc -> {

								if (!ptdc.getNro_cuenta().equals(null)) {
									cta.setSaldo(cta.getSaldo() - mov.getMonto());
									return repo1.save(cta).flatMap(ncta -> {
										return repoMov.save(mov);
									});
								} else {
									return Mono.just(new Movimientos());
								}

							});
				} else {
					return Mono.just(new Movimientos());
				}
			}
			case "ecorriente": {
				if (cta.getSaldo() >= mov.getMonto()) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("nro_cuenta", mov.getCuentaTo());
					params.put("cuentaFrom", mov.getNro_cuenta());
					params.put("descripcion", "transferencia");
					params.put("monto", mov.getMonto());
					params.put("fecha", mov.getFecha());

					return ecorriente.post().uri("/getTransferEcorriente").accept(MediaType.APPLICATION_JSON_UTF8)
							.body(BodyInserters.fromObject(params)).retrieve().bodyToMono(Movimientos.class)
							.flatMap(ptdc -> {

								if (!ptdc.getNro_cuenta().equals(null)) {
									cta.setSaldo(cta.getSaldo() - mov.getMonto());
									return repo1.save(cta).flatMap(ncta -> {
										return repoMov.save(mov);
									});
								} else {
									return Mono.just(new Movimientos());
								}

							});
				} else {
					return Mono.just(new Movimientos());
				}
			}
			case "corrientevip": {
				if (cta.getSaldo() >= mov.getMonto()) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("nro_cuenta", mov.getCuentaTo());
					params.put("cuentaFrom", mov.getNro_cuenta());
					params.put("descripcion", "transferencia");
					params.put("monto", mov.getMonto());
					params.put("fecha", mov.getFecha());

					return vip.post().uri("/getTransferCorrienteVip").accept(MediaType.APPLICATION_JSON_UTF8)
							.body(BodyInserters.fromObject(params)).retrieve().bodyToMono(Movimientos.class)
							.flatMap(ptdc -> {

								if (!ptdc.getNro_cuenta().equals(null)) {
									cta.setSaldo(cta.getSaldo() - mov.getMonto());
									return repo1.save(cta).flatMap(ncta -> {
										return repoMov.save(mov);
									});
								} else {
									return Mono.just(new Movimientos());
								}

							});
				} else {
					return Mono.just(new Movimientos());
				}
			}
			default: {
				return Mono.just(new Movimientos());
			}

			}
		});
	}

}

package com.everis.bc.servicioCuentaCorrienteP.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${valores.comision}")
	private double comision;
	@Value("${valores.movesxmonth}")
	private int movesxmonth;

	//private List<String> docs;

	@Override
	public Mono<CuentaCorrienteP> saveData(CuentaCorrienteP cuenta) {
		//Map<String, Object> respuesta = new HashMap<String, Object>();

		List<String> doc = new ArrayList<>();
		for (Listas h : cuenta.getTitulares()) {
			doc.add(h.getDoc());
		}

		return repo1.findByTitularesDocList(doc).flatMap(ctas -> {
			if(!ctas.getBankcode().equals(cuenta.getBankcode())) {
				return repo1.save(cuenta);
			}else {
				return Mono.just(ctas);
			}
			
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
		/*return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {

			cta.setSaldo(cta.getSaldo() + mov.getMonto());
			return repo1.save(cta).flatMap(ncta -> {
				return repoMov.save(mov);
			});
		});*/
		return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {
			//-----------------valida si tiene movimientos disponibles en el mes-----------------//
			if (cta.getLastmove().getMonth() == mov.getFecha().getMonth() && cta.getMovesxmonth() > 0) {
				Double saldo = cta.getSaldo();
				cta.setSaldo(saldo + mov.getMonto());
				cta.setMovesxmonth(cta.getMovesxmonth() - 1);
				cta.setLastmove(mov.getFecha());
				return repo1.save(cta).flatMap(ncta->{
					return repoMov.save(mov);
				});
				
				
			} else {
			//-si el mes de la transaccion es distinto reinicia la cantidad de movimientos por mes-//
				if (cta.getLastmove().getMonth() != mov.getFecha().getMonth()) {
					Double saldo = cta.getSaldo();
					cta.setSaldo(saldo + mov.getMonto());
					cta.setLastmove(mov.getFecha());
					cta.setMovesxmonth(4);
					return repo1.save(cta).flatMap(ncta->{
						return repoMov.save(mov);
					});
					
			//--si no tiene movimientos disponibles en el mes aplica el cobro de comision--//
				} else {
					Double saldo = cta.getSaldo();
					//Double comision = 15.00;
					
					if ((saldo+mov.getMonto()) - comision >=0) {
						cta.setSaldo(saldo + mov.getMonto() - comision);
						cta.setLastmove(mov.getFecha());
						mov.setComision(comision);
						return repo1.save(cta).flatMap(ncta->{
							return repoMov.save(mov);
						});
						
					} else {
						return Mono.just(new Movimientos());
					}
				}
			}
		});
	}

	@Override
	public Mono<Movimientos> saveRetiro(Movimientos mov) {
		// TODO Auto-generated method stub
		/*return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {
			if (cta.getSaldo() >= mov.getMonto()) {
				cta.setSaldo(cta.getSaldo() - mov.getMonto());
				return repo1.save(cta).flatMap(ncta -> {
					return repoMov.save(mov);
				});
			} else {
				return Mono.just(new Movimientos());
			}
		});*/
		return repo1.findByNro_cuenta(mov.getNro_cuenta()).flatMap(cta -> {
			//-----------------valida si tiene movimientos disponibles en el mes-----------------//
			if (cta.getLastmove().getMonth() == mov.getFecha().getMonth() && cta.getMovesxmonth() > 0) {
				Double saldo = cta.getSaldo();
				cta.setSaldo(saldo + mov.getMonto());
				cta.setLastmove(mov.getFecha());
				cta.setMovesxmonth(cta.getMovesxmonth() - 1);
				if (saldo >= mov.getMonto()) {
					cta.setSaldo(saldo - mov.getMonto());
					return repo1.save(cta).flatMap(ncta->{
						return repoMov.save(mov);
					});
					
				} else {
					return Mono.just(new Movimientos());
				}

			} else {
			//-si el mes de la transaccion es distinto reinicia la cantidad de movimientos por mes-//
				if (cta.getLastmove().getMonth() != mov.getFecha().getMonth()) {
					Double saldo = cta.getSaldo();
					cta.setSaldo(saldo + mov.getMonto());
					if (saldo >= mov.getMonto()) {
						cta.setSaldo(saldo - mov.getMonto());
						cta.setLastmove(mov.getFecha());
						cta.setMovesxmonth(4);
						return repo1.save(cta).flatMap(ncta->{
							return repoMov.save(mov);
						});
						
					} else {
						return Mono.just(new Movimientos());
					}
		//--si no tiene movimientos disponibles en el mes aplica el cobro de comision--//
				} else {
					Double saldo = cta.getSaldo();
					//Double comision = 15.0;
					cta.setSaldo(saldo + mov.getMonto());
					cta.setLastmove(mov.getFecha());
					if (saldo >= mov.getMonto() + comision) {
						cta.setSaldo(saldo - mov.getMonto() - comision);
						return repo1.save(cta).flatMap(ncta->{
							return repoMov.save(mov);
						});
						
					} else {
						return Mono.just(new Movimientos());
					}
				}
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
	
	@Override
	public Flux<Movimientos> getRangeMovimientos(String nro_cuenta, String from, String to) {
		// TODO Auto-generated method stub
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		 try {
			
			Date first = df.parse(from);
			Date last = df.parse(to);
			System.out.println(first.toString()+" "+last);
			
			//return repoMov.findAllDateRangeByNro_cuenta(nro_cuenta, first, last);
			return repoMov.findByFechaBetween(first, last).filter(moves->moves.getNro_cuenta().equals(nro_cuenta));
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		
	}

}

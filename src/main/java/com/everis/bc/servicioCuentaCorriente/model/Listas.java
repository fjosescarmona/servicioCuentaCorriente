package com.everis.bc.servicioCuentaCorriente.model;

import javax.validation.constraints.NotNull;

public class Listas {
	@NotNull
	private String doc="";
	@NotNull
	private String nombre="";
	@NotNull
	private String Apellido="";
	@NotNull
	private String producto="";

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return Apellido;
	}

	public void setApellido(String apellido) {
		Apellido = apellido;
	}

	public String getProducto() {
		return producto;
	}

	public void setProducto(String producto) {
		this.producto = producto;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}
}

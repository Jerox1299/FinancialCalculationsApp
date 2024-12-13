package com.example.financialcalculationsapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CalculadoraViewModel(application: Application) : AndroidViewModel(application) {

    private val _resultadoLiveData = MutableLiveData<String>()
    val resultadoLiveData: LiveData<String> = _resultadoLiveData

    private val _historialLiveData = MutableLiveData<List<String>>()
    val historialLiveData: LiveData<List<String>> = _historialLiveData

    private val historial = mutableListOf<String>()
    private val sharedPreferences =
        application.getSharedPreferences("CalculosFinancieros", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        cargarHistorial()
    }

    // Cálculos de Productos
    fun calcularPrecioConIVA(precioBase: Double) {
        val precioConIVA = precioBase * 1.19
        val resultado = "Precio con IVA: $${String.format("%.2f", precioConIVA)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularMargenGanancia(precioVenta: Double, costo: Double) {
        val margen = ((precioVenta - costo) / precioVenta) * 100
        val resultado = "Margen de ganancia: ${String.format("%.2f", margen)}%"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularPuntoEquilibrio(
        costosFijos: Double,
        precioVentaUnitario: Double,
        costoVariableUnitario: Double
    ) {
        val puntoEquilibrio = costosFijos / (precioVentaUnitario - costoVariableUnitario)
        val resultado = "Punto de equilibrio: ${String.format("%.2f", puntoEquilibrio)} unidades"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularROI(ingresos: Double, inversion: Double) {
        val roi = ((ingresos - inversion) / inversion) * 100
        val resultado = "ROI: ${String.format("%.2f", roi)}%"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    // Cálculos de Empleador
    fun calcularCostoTotalNomina(salarioBase: Double) {
        val aportesParafiscales = salarioBase * 0.09
        val seguridadSocial = salarioBase * 0.205
        val prestaciones = salarioBase * 0.2183
        val costoTotal = salarioBase + aportesParafiscales + seguridadSocial + prestaciones
        val resultado = "Costo total de nómina: $${String.format("%.2f", costoTotal)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularProvisionesSociales(salarioBase: Double) {
        val provisiones = salarioBase * 0.2183
        val resultado = "Provisiones sociales: $${String.format("%.2f", provisiones)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularAportesParafiscales(salarioBase: Double) {
        val aportes = salarioBase * 0.09
        val resultado = "Aportes parafiscales: $${String.format("%.2f", aportes)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularPrestacionesSociales(salarioBase: Double) {
        val prestaciones = salarioBase * 0.2183
        val resultado = "Prestaciones sociales: $${String.format("%.2f", prestaciones)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    // Cálculos de Empleado
    fun calcularSalarioNeto(salarioBase: Double) {
        val deducciones = salarioBase * 0.08
        val salarioNeto = salarioBase - deducciones
        val resultado = "Salario neto: $${String.format("%.2f", salarioNeto)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularDeduccionesNomina(salarioBase: Double) {
        val deducciones = salarioBase * 0.08
        val resultado = "Deducciones de nómina: $${String.format("%.2f", deducciones)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularHoraExtra(salarioBase: Double, tipo: Int) {
        val valorHora = salarioBase / 240
        val valorHoraExtra = when (tipo) {
            1 -> valorHora * 1.25 // diurna
            2 -> valorHora * 1.75 // nocturna
            3, 4 -> valorHora * 2 // dominical o festiva
            else -> valorHora
        }
        val tipoString = when (tipo) {
            1 -> "diurna"
            2 -> "nocturna"
            3 -> "dominical"
            4 -> "festiva"
            else -> "desconocida"
        }
        val resultado = "Valor hora extra $tipoString: $${String.format("%.2f", valorHoraExtra)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    fun calcularBonificacion(salarioBase: Double, porcentajeBonificacion: Double) {
        val bonificacion = salarioBase * (porcentajeBonificacion / 100)
        val resultado = "Bonificación: $${String.format("%.2f", bonificacion)}"
        _resultadoLiveData.value = resultado
        agregarAlHistorial(resultado)
    }

    private fun agregarAlHistorial(resultado: String) {
        historial.add(0, resultado)
        if (historial.size > 5) {
            historial.removeAt(historial.size - 1)
        }
        _historialLiveData.value = historial.toList()
        guardarHistorial()
    }

    private fun cargarHistorial() {
        val historialJson = sharedPreferences.getString("historial", null)
        if (historialJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            historial.clear()
            historial.addAll(gson.fromJson(historialJson, type))
            _historialLiveData.value = historial.toList()
        }
    }

    private fun guardarHistorial() {
        val historialJson = gson.toJson(historial)
        sharedPreferences.edit().putString("historial", historialJson).apply()
    }

    // Validaciones
    fun validarDatosProductos(precioBase: Double?, precioVenta: Double?, costo: Double?): Boolean {
        return precioBase != null && precioBase > 0 && (precioVenta == null || precioVenta > 0) && (costo == null || costo > 0)
    }

    fun validarDatosEmpleador(salarioBase: Double?): Boolean {
        return salarioBase != null && salarioBase >= 908526 // Salario mínimo en Colombia 2023
    }

    fun validarDatosEmpleado(salarioBase: Double?): Boolean {
        return salarioBase != null && salarioBase > 0
    }
}

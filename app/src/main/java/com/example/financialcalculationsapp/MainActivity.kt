package com.example.financialcalculationsapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.financialcalculationsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CalculadoraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CalculadoraViewModel::class.java)

        setupSpinners()
        setupCalculateButton()
        observeViewModel()
    }

    private fun setupSpinners() {
        val categorias = arrayOf("Productos", "Empleador", "Empleado")
        val adapterCategorias = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        binding.spinnerCategoria.adapter = adapterCategorias

        binding.spinnerCategoria.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    updateTipoCalculoSpinner(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun updateTipoCalculoSpinner(categoriaPosition: Int) {
        val tiposCalculo = when (categoriaPosition) {
            0 -> arrayOf("Precio con IVA", "Margen de ganancia", "Punto de equilibrio", "ROI")
            1 -> arrayOf(
                "Costo total de nómina",
                "Provisiones sociales",
                "Aportes parafiscales",
                "Prestaciones sociales"
            )

            2 -> arrayOf("Salario neto", "Deducciones de nómina", "Horas extras", "Bonificaciones")
            else -> arrayOf()
        }
        val adapterTiposCalculo =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposCalculo)
        binding.spinnerTipoCalculo.adapter = adapterTiposCalculo

        // Actualizar visibilidad de los campos de entrada
        updateInputFieldsVisibility(categoriaPosition, 0)

        binding.spinnerTipoCalculo.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    updateInputFieldsVisibility(
                        binding.spinnerCategoria.selectedItemPosition,
                        position
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun updateInputFieldsVisibility(categoria: Int, tipoCalculo: Int) {
        binding.inputLayout1.visibility = View.VISIBLE
        binding.inputLayout2.visibility = View.GONE
        binding.inputLayout3.visibility = View.GONE

        when (categoria) {
            0 -> { // Productos
                when (tipoCalculo) {
                    0 -> { // Precio con IVA
                        binding.inputLayout1.hint = "Precio base"
                    }

                    1 -> { // Margen de ganancia
                        binding.inputLayout1.hint = "Precio venta"
                        binding.inputLayout2.visibility = View.VISIBLE
                        binding.inputLayout2.hint = "Costo"
                    }

                    2 -> { // Punto de equilibrio
                        binding.inputLayout1.hint = "Costos fijos"
                        binding.inputLayout2.visibility = View.VISIBLE
                        binding.inputLayout2.hint = "Precio venta unitario"
                        binding.inputLayout3.visibility = View.VISIBLE
                        binding.inputLayout3.hint = "Costo variable unitario"
                    }

                    3 -> { // ROI
                        binding.inputLayout1.hint = "Ingresos"
                        binding.inputLayout2.visibility = View.VISIBLE
                        binding.inputLayout2.hint = "Inversión"
                    }
                }
            }

            1 -> { // Empleador
                binding.inputLayout1.hint = "Salario base"
            }

            2 -> { // Empleado
                when (tipoCalculo) {
                    0, 1 -> { // Salario neto, Deducciones de nómina
                        binding.inputLayout1.hint = "Salario base"
                    }

                    2 -> { // Horas extras
                        binding.inputLayout1.hint = "Salario base"
                        binding.inputLayout2.visibility = View.VISIBLE
                        binding.inputLayout2.hint =
                            "Tipo (1: diurna / 2: nocturna/ 3: dominical o festivo)"
                    }

                    3 -> { // Bonificaciones
                        binding.inputLayout1.hint = "Salario base"
                        binding.inputLayout2.visibility = View.VISIBLE
                        binding.inputLayout2.hint = "Porcentaje de bonificación"
                    }
                }
            }
        }
    }

    private fun setupCalculateButton() {
        binding.buttonCalcular.setOnClickListener {
            val categoria = binding.spinnerCategoria.selectedItemPosition
            val tipoCalculo = binding.spinnerTipoCalculo.selectedItemPosition
            val valor1 = binding.editTextValor1.text.toString().toDoubleOrNull()
            val valor2 = binding.editTextValor2.text.toString().toDoubleOrNull()
            val valor3 = binding.editTextValor3.text.toString().toDoubleOrNull()

            when (categoria) {
                0 -> calcularProductos(tipoCalculo, valor1, valor2, valor3)
                1 -> calcularEmpleador(tipoCalculo, valor1)
                2 -> calcularEmpleado(tipoCalculo, valor1, valor2)
            }
        }
    }

    private fun calcularProductos(
        tipoCalculo: Int,
        valor1: Double?,
        valor2: Double?,
        valor3: Double?
    ) {
        if (!viewModel.validarDatosProductos(valor1, valor2, valor3)) {
            mostrarError("Datos inválidos para cálculos de productos")
            return
        }

        when (tipoCalculo) {
            0 -> viewModel.calcularPrecioConIVA(valor1!!)
            1 -> viewModel.calcularMargenGanancia(valor1!!, valor2!!)
            2 -> viewModel.calcularPuntoEquilibrio(valor1!!, valor2!!, valor3!!)
            3 -> viewModel.calcularROI(valor1!!, valor2!!)
        }
    }

    private fun calcularEmpleador(tipoCalculo: Int, salarioBase: Double?) {
        if (!viewModel.validarDatosEmpleador(salarioBase)) {
            mostrarError("Salario base inválido para cálculos de empleador")
            return
        }

        when (tipoCalculo) {
            0 -> viewModel.calcularCostoTotalNomina(salarioBase!!)
            1 -> viewModel.calcularProvisionesSociales(salarioBase!!)
            2 -> viewModel.calcularAportesParafiscales(salarioBase!!)
            3 -> viewModel.calcularPrestacionesSociales(salarioBase!!)
        }
    }

    private fun calcularEmpleado(tipoCalculo: Int, salarioBase: Double?, valor2: Double?) {
        if (!viewModel.validarDatosEmpleado(salarioBase)) {
            mostrarError("Salario base inválido para cálculos de empleado")
            return
        }

        when (tipoCalculo) {
            0 -> viewModel.calcularSalarioNeto(salarioBase!!)
            1 -> viewModel.calcularDeduccionesNomina(salarioBase!!)
            2 -> viewModel.calcularHoraExtra(salarioBase!!, valor2?.toInt() ?: 0)
            3 -> viewModel.calcularBonificacion(salarioBase!!, valor2 ?: 0.0)
        }
    }

    private fun observeViewModel() {
        viewModel.resultadoLiveData.observe(this) { resultado ->
            binding.textViewResultado.text = resultado
        }

        viewModel.historialLiveData.observe(this) { historial ->
            binding.textViewHistorial.text = historial.joinToString("\n")
        }
    }

    private fun mostrarError(mensaje: String) {
        binding.textViewResultado.text = "Error: $mensaje"
    }
}

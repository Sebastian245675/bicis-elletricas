package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;

public class JPanelInstructions extends JPanel implements JPanelView {
    
    private static final Logger LOGGER = Logger.getLogger(JPanelInstructions.class.getName());
    
    private final AppView m_App;
    private final JEditorPane m_editor;
    private final JScrollPane m_scroll;
    
    private static final Color COLOR_BACKGROUND = new Color(250, 247, 242); // Hex #FAF7F2
    private static final Color COLOR_ACCENT = new Color(202, 159, 65); // Hex #CA9F41
    
    public JPanelInstructions(AppView app) {
        this.m_App = app;
        
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Editor pane para HTML
        m_editor = new JEditorPane();
        m_editor.setEditable(false);
        m_editor.setContentType("text/html");
        
        // Soporte para navegación con enlaces de ancla locales (#seccion)
        m_editor.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    String desc = e.getDescription();
                    if (desc != null && desc.startsWith("#")) {
                        m_editor.scrollToReference(desc.substring(1));
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error al navegar por enlace interno: " + ex.getMessage());
                }
            }
        });
        
        // HTML con estilos CSS embebidos para un diseño PREMIUM
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Segoe UI, sans-serif; background-color: #FAF7F2; margin: 20px; color: #334155;'>");
        
        // Encabezado principal
        html.append("<table width='100%' border='0' cellpadding='20' cellspacing='0' style='background-color: #CA9F41; margin-bottom: 15px; border-radius: 6px;'>");
        html.append("<tr><td>");
        html.append("<h1 style='margin: 0; font-size: 32px; color: #ffffff;'>Voltium Sanrey</h1>");
        html.append("<p style='margin: 5px 0 0 0; font-size: 16px; color: #ffffff; opacity: 0.9;'>Manual de Usuario y Guía Completa de Operación del Sistema POS</p>");
        html.append("</td></tr>");
        html.append("</table>");
        
        // Barra de navegación por secciones (Índice Rápido)
        html.append("<table width='100%' border='0' cellpadding='6' cellspacing='4' style='background-color: #E2D9C8; margin-bottom: 25px; border-radius: 4px;'>");
        html.append("<tr align='center'>");
        html.append("<td><a href='#shortcuts' style='color: #473209; text-decoration: none; font-weight: bold;'>⌨️ Atajos</a></td>");
        html.append("<td><a href='#ventas' style='color: #473209; text-decoration: none; font-weight: bold;'>🛒 Ventas y Cobros</a></td>");
        html.append("<td><a href='#fidelizacion' style='color: #473209; text-decoration: none; font-weight: bold;'>🎁 Puntos</a></td>");
        html.append("<td><a href='#apartados' style='color: #473209; text-decoration: none; font-weight: bold;'>🚲 Apartados</a></td>");
        html.append("<td><a href='#cierres' style='color: #473209; text-decoration: none; font-weight: bold;'>📊 Arqueos y Cierres</a></td>");
        html.append("<td><a href='#inventario' style='color: #473209; text-decoration: none; font-weight: bold;'>📦 Inventarios</a></td>");
        html.append("<td><a href='#alertas' style='color: #473209; text-decoration: none; font-weight: bold;'>✉️ Alertas y Emails</a></td>");
        html.append("<td><a href='#faq' style='color: #473209; text-decoration: none; font-weight: bold;'>❓ FAQ</a></td>");
        html.append("</tr>");
        html.append("</table>");
        
        // --- SECCIÓN 1: ATAJOS DE TECLADO ---
        html.append("<h2><a name='shortcuts'></a><font color='#CA9F41'>⌨️ Atajos de Teclado del Sistema</font></h2>");
        html.append("<p style='font-size: 13px;'>Optimice el tiempo de atención utilizando los atajos de teclado configurados en los diferentes módulos del POS:</p>");
        
        // Tabla de atajos globales
        html.append("<table width='100%' border='0' cellpadding='8' cellspacing='1' bgcolor='#E2E8F0' style='margin-bottom: 15px;'>");
        html.append("<tr bgcolor='#1E293B'>");
        html.append("<th colspan='2' align='left' style='padding: 8px;'><font color='#ffffff'><b>Atajos Globales (Cualquier Pantalla)</b></font></th>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td width='15%' style='padding: 8px;'><b><font color='#CA9F41'>F1</font></b></td>");
        html.append("<td style='padding: 8px;'>Ir inmediatamente al módulo de <b>Ventas / Facturación</b>.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F2</font></b></td>");
        html.append("<td style='padding: 8px;'>Ir inmediatamente a <b>Cerrar Caja / Turnos</b> (Arqueo y conciliaciones).</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F3</font></b></td>");
        html.append("<td style='padding: 8px;'>Ir inmediatamente a <b>Stock / Gestión de Inventario</b> (Productos y almacenes).</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F4</font></b></td>");
        html.append("<td style='padding: 8px;'>Ir inmediatamente a <b>Reportes y Gráficos</b> (Análisis de ventas y KPI).</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>Esc</font></b></td>");
        html.append("<td style='padding: 8px;'>Regresa a la pantalla de <b>Inicio / Portada (Índice General)</b> desde cualquier sección.</td>");
        html.append("</tr>");
        html.append("</table>");
        
        // Tabla de atajos en ventas
        html.append("<table width='100%' border='0' cellpadding='8' cellspacing='1' bgcolor='#E2E8F0' style='margin-bottom: 25px;'>");
        html.append("<tr bgcolor='#1E293B'>");
        html.append("<th colspan='2' align='left' style='padding: 8px;'><font color='#ffffff'><b>Atajos Exclusivos del Módulo de Ventas / Facturación</b></font></th>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td width='15%' style='padding: 8px;'><b><font color='#CA9F41'>F2</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Corte de Caja Rápido</b>: Carga el panel para arqueo del cajero activo en el turno.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F3</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Pestañas en Espera</b>: Activa el historial de pestañas y ventas activas en paralelo.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F4</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Nueva Pestaña de Venta</b>: Crea un ticket en blanco para atender a otro cliente.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F5</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Asignar Cliente</b>: Despliega el buscador y registro de clientes en el ticket.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F6</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Eliminar Línea</b>: Remueve el producto seleccionado actualmente de la lista de compra.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F7</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Entrada de Efectivo</b>: Registra ingresos manuales a caja (ej. fondo inicial, cambio).</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F8</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Salida de Efectivo</b>: Registra retiros de dinero de caja (gastos menores o resguardos).</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F9</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Atributos de Producto</b>: Abre la ventana para registrar número de serie, color o lote.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F10</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Apartar Vehículo</b>: Abre la interfaz para registrar un apartado (Layaway) del producto seleccionado.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>Shift + F10</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Dividir Ticket</b>: Divide la cuenta actual entre varios clientes o formas de pago.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>F12</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Cobrar Cuenta</b>: Simula el botón de cobrar y despliega la ventana de métodos de pago.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>Ctrl+Shift+U</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Editar Cantidad</b>: Edita rápidamente las unidades/cantidad del artículo seleccionado.</td>");
        html.append("</tr>");
        html.append("<tr bgcolor='#ffffff'>");
        html.append("<td style='padding: 8px;'><b><font color='#CA9F41'>C</font></b></td>");
        html.append("<td style='padding: 8px;'><b>Reimprimir Ticket</b>: Genera una copia impresa del último ticket cobrado.</td>");
        html.append("</tr>");
        html.append("</table>");
        
        // --- SECCIÓN 2: VENTAS Y COBROS ---
        html.append("<h2><a name='ventas'></a><font color='#CA9F41'>🛒 Flujo de Ventas y Métodos de Cobro</font></h2>");
        html.append("<table width='100%' border='0' cellpadding='15' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2D9C8; margin-bottom: 25px;'>");
        html.append("<tr><td>");
        html.append("<p style='margin: 0; font-size: 13.5px; line-height: 1.6; color: #475569;'>");
        html.append("<b>1. Captura de Artículos:</b> Pase el código de barras por el lector, busque el artículo escribiendo su nombre en la barra de búsqueda rápida del módulo de ventas, o selecciónelo directamente desde la botonera del menú inferior.<br><br>");
        html.append("<b>2. Venta Simultánea (Pestañas):</b> Si un cliente requiere buscar otro artículo, puede dejar su venta 'en espera' presionando <b>F4</b> para iniciar un ticket limpio. Presione <b>F3</b> para ver y retomar cualquiera de las pestañas anteriores.<br><br>");
        html.append("<b>3. Identificación del Cliente:</b> Utilice <b>F5</b> antes del cobro para asociar un cliente. Es obligatorio para <b>ventas a crédito, acumulación/pago con puntos, facturación CFDI o registro de apartados</b>.<br><br>");
        html.append("<b>4. Métodos de Pago Disponibles (F12):</b> El sistema cuenta con opciones de cobro flexibles:<br>");
        html.append("&nbsp;&nbsp;• <b>Efectivo:</b> Permite registrar el importe pagado y muestra automáticamente el cambio exacto a entregar.<br>");
        html.append("&nbsp;&nbsp;• <b>Tarjeta:</b> Para registrar transacciones cobradas por terminal bancaria (Crédito/Débito).<br>");
        html.append("&nbsp;&nbsp;• <b>Vales:</b> Cobro mediante vales de despensa físicos o electrónicos.<br>");
        html.append("&nbsp;&nbsp;• <b>Crédito:</b> Registra la venta en la cuenta corriente del cliente (requiere autorización previa de crédito en su ficha).<br>");
        html.append("&nbsp;&nbsp;• <b>Puntos:</b> Permite liquidar el total o parcial de la compra utilizando el saldo acumulado en el programa de lealtad.<br><br>");
        html.append("<b>5. Facturación CFDI:</b> Al finalizar la venta con un cliente que tiene RFC registrado, el sistema genera de forma transparente la información requerida para el XML y la factura CFDI membretada.");
        html.append("</p></td></tr></table>");
        
        // --- SECCIÓN 3: FIDELIZACIÓN (PUNTOS) ---
        html.append("<h2><a name='fidelizacion'></a><font color='#CA9F41'>🎁 Programa de Fidelización (Puntos de Regalo)</font></h2>");
        html.append("<table width='100%' border='0' cellpadding='15' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2D9C8; margin-bottom: 25px;'>");
        html.append("<tr><td>");
        html.append("<p style='margin: 0; font-size: 13.5px; line-height: 1.6; color: #475569;'>");
        html.append("El programa premia la lealtad de sus compradores mediante la acumulación automática de puntos de compra:<br><br>");
        html.append("• <b>Regla de Acumulación:</b> Por cada <b>$400.00 MXN</b> de compra neta en un solo ticket, el sistema acumula automáticamente <b>10 puntos</b> en la cuenta del cliente (ejemplo: un ticket de $1,250.00 MXN acumula 30 puntos).<br>");
        html.append("• <b>Asociación de Cliente:</b> Para acumular, es indispensable presionar <b>F5</b> y seleccionar al cliente en la venta antes de procesar el pago.<br>");
        html.append("• <b>Consulta de Saldo:</b> Una vez seleccionado el cliente en el panel de ventas, sus puntos acumulados totales se mostrarán de forma inmediata en la parte superior derecha de la pantalla.<br>");
        html.append("• <b>Redención en Cobros:</b> En el menú de pagos (F12), presione el botón <b>Puntos</b> para liquidar el importe del ticket. El sistema convertirá los puntos disponibles del cliente en saldo monetario para el pago.");
        html.append("</p></td></tr></table>");
        
        // --- SECCIÓN 4: APARTADOS Y CRÉDITOS ---
        html.append("<h2><a name='apartados'></a><font color='#CA9F41'>🚲 Apartado de Vehículos y Planes de Pago</font></h2>");
        html.append("<table width='100%' border='0' cellpadding='15' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2D9C8; margin-bottom: 25px;'>");
        html.append("<tr><td>");
        html.append("<p style='margin: 0; font-size: 13.5px; line-height: 1.6; color: #475569;'>");
        html.append("Voltium Sanrey cuenta con un flujo especializado de apartados (Layaway) para apartar motocicletas, scooters y bicicletas:<br><br>");
        html.append("<b>1. Registrar Apartado:</b> Cargue el vehículo en el ticket de ventas, asigne al cliente (<b>F5</b>) y presione <b>F10</b> (Apartar Vehículo). El sistema solicitará registrar los datos de enganche.<br>");
        html.append("<b>2. Information Específica:</b> Recuerde capturar el <b>Número de Serie (chasis/motor)</b> y el <b>Color</b> del vehículo utilizando el botón de atributos (<b>F9</b>) antes de formalizar el apartado.<br>");
        html.append("<b>3. Abonos y Cuotas:</b> El cliente puede efectuar abonos sucesivos en el panel de apartados. El sistema registrará cada pago e imprimirá el ticket de abono correspondiente.<br>");
        html.append("<b>4. Liquidación y Stock:</b> El inventario físico del vehículo se mantiene apartado (reservado). Únicamente cuando la cuenta se liquide al 100%, el sistema permitirá marcar el artículo como <i>Entregado</i>, dando de baja definitiva el stock en el almacén.<br>");
        html.append("<b>5. Alertas de Vencimiento:</b> Si el cliente supera la fecha límite de pago o se retrasa en sus cuotas periódicas, el sistema emitirá alertas automáticas visibles para el administrador y programará un correo electrónico de notificación.");
        html.append("</p></td></tr></table>");
        
        // --- SECCIÓN 5: ARQUEOS Y CIERRES DE CAJA ---
        html.append("<h2><a name='cierres'></a><font color='#CA9F41'>📊 Cortes de Turno y Conciliación Física de Caja</font></h2>");
        html.append("<table width='100%' border='0' cellpadding='15' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2D9C8; margin-bottom: 25px;'>");
        html.append("<tr><td>");
        html.append("<p style='margin: 0; font-size: 13.5px; line-height: 1.6; color: #475569;'>");
        html.append("Para garantizar el control del efectivo en caja, siga estrictamente el siguiente proceso de cierre:<br><br>");
        html.append("<b>1. Iniciar Corte (F2):</b> Al terminar su turno, presione <b>F2</b> o vaya al menú <i>Cerrar Caja / Turnos</i>.<br><br>");
        html.append("<b>2. Conciliación Física Obligatoria:</b> Al solicitar el cierre, se abrirá un panel donde deberá capturar la cantidad total de dinero físico contado directamente en el cajón de efectivo (monedas y billetes).<br><br>");
        html.append("<b>3. Detección de Diferencias (Sobrantes/Faltantes):</b> El sistema compara el conteo ingresado contra las operaciones registradas (Ventas + Fondo de caja inicial + Entradas manuales - Salidas manuales). Calculará de inmediato y reportará si existe un:<br>");
        html.append("&nbsp;&nbsp;• <font color='green'><b>Sobrante:</b></font> Dinero físico mayor al esperado por el sistema.<br>");
        html.append("&nbsp;&nbsp;• <font color='red'><b>Faltante:</b></font> Dinero físico menor al esperado por el sistema (alerta de auditoría).<br><br>");
        html.append("<b>4. Cierre del Día:</b> El administrador del sistema consolida la información de todos los turnos del día natural para generar el balance global de ingresos.<br><br>");
        html.append("<b>5. Cierre de Mes (Cerrar Mes):</b> Esta herramienta realiza una consolidación masiva de todos los turnos del mes calendario. Genera estadísticas agregadas de ventas por departamento, egresos, impuestos recolectados e ingresos netos para exportación contable.");
        html.append("</p></td></tr></table>");
        
        // --- SECCIÓN 6: INVENTARIOS ---
        html.append("<h2><a name='inventario'></a><font color='#CA9F41'>📦 Ficha de Productos y Búsqueda Inteligente</font></h2>");
        html.append("<table width='100%' border='0' cellpadding='15' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2D9C8; margin-bottom: 25px;'>");
        html.append("<tr><td>");
        html.append("<p style='margin: 0; font-size: 13.5px; line-height: 1.6; color: #475569;'>");
        html.append("<b>Campos Especializados de Inventario:</b> En el catálogo de productos (<i>Stock > Productos</i>), dispondrá de campos diseñados para vehículos y refacciones, tales como:<br>");
        html.append("&nbsp;&nbsp;• <b>Modelo:</b> Modelo comercial del vehículo (ej. Voltium 1500, Sanrey City).<br>");
        html.append("&nbsp;&nbsp;• <b>Color:</b> Color del chasis o plástico del vehículo.<br>");
        html.append("&nbsp;&nbsp;• <b>Voltaje:</b> Configuración eléctrica (ej. 48V, 60V, 72V).<br>");
        html.append("&nbsp;&nbsp;• <b>Número de Serie / Chasis:</b> Identificador único grabado para control de garantías y propiedad.<br>");
        html.append("&nbsp;&nbsp;• <b>Lote:</b> Control de cargamento y fecha de importación.<br><br>");
        html.append("<b>Buscador Inteligente de Portada:</b> En el menú de Inicio, escriba cualquier atributo (por ejemplo, <i>'60V'</i>, el número de serie de un motor, o <i>'Rojo'</i>) en el buscador superior. El sistema filtrará instantáneamente los módulos relacionados y le permitirá ingresar directamente con los parámetros pre-cargados.<br><br>");
        html.append("<b>Stock Mínimo y Alertas:</b> Configure la cantidad mínima recomendada en la ficha de cada artículo. Al caer por debajo de este límite, el artículo se marcará en rojo en las consultas de stock y se incluirá en el reporte de reabastecimiento.");
        html.append("</p></td></tr></table>");
        
        // --- SECCIÓN 7: ALERTAS Y EMAILS ---
        html.append("<h2><a name='alertas'></a><font color='#CA9F41'>✉️ Notificaciones Automáticas por Correo</font></h2>");
        html.append("<table width='100%' border='0' cellpadding='15' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2D9C8; margin-bottom: 25px;'>");
        html.append("<tr><td>");
        html.append("<p style='margin: 0; font-size: 13.5px; line-height: 1.6; color: #475569;'>");
        html.append("El sistema cuenta con un planificador nocturno y periódico en segundo plano que envía alertas a los correos configurados:<br><br>");
        html.append("• <b>Alerta de Stock Bajo:</b> Al terminar el día, si un producto cae por debajo de su stock mínimo, se envía un correo con el listado detallado de artículos y cantidades necesarias para compra.<br>");
        html.append("• <b>Alerta de Apartados Vencidos:</b> Envío diario de notificaciones sobre planes de apartados vencidos o clientes con cuotas atrasadas, facilitando la labor de cobranza.<br>");
        html.append("• <b>Reportes Programados:</b> Envío automático de balances semanales y mensuales en formato HTML con gráficos de ventas, facturas emitidas y recaudación por método de pago.");
        html.append("</p></td></tr></table>");
        
        // --- SECCIÓN 8: PREGUNTAS FRECUENTES (FAQ) ---
        html.append("<h2><a name='faq'></a><font color='#CA9F41'>❓ Preguntas Frecuentes (FAQ)</font></h2>");
        html.append("<table width='100%' border='0' cellpadding='15' cellspacing='0' style='background-color: #ffffff; border: 1px solid #E2D9C8; margin-bottom: 25px;'>");
        html.append("<tr><td>");
        html.append("<p style='margin: 0; font-size: 13px; line-height: 1.6; color: #475569;'>");
        html.append("<b>P: ¿Por qué la opción de cobro con 'Puntos' aparece deshabilitada?</b><br>");
        html.append("R: Se debe a dos razones comunes: 1) No ha asignado un cliente al ticket (use <b>F5</b>), o 2) El cliente seleccionado no cuenta con puntos acumulados suficientes para aplicar al pago.<br><br>");
        html.append("<b>P: ¿Cómo registro un gasto menor de la caja?</b><br>");
        html.append("R: En la pantalla de ventas, presione <b>F8</b>, introduzca el concepto (ej. 'Limpieza', 'Papelería') y el monto retirado. Esto evitará descuadres de dinero en la conciliación física al hacer el corte.<br><br>");
        html.append("<b>P: ¿Qué hago si tengo una diferencia de faltante al cerrar caja?</b><br>");
        html.append("R: Verifique si olvidó registrar una salida de caja (<b>F8</b>) o si capturó incorrectamente un pago con tarjeta como efectivo. El sistema registrará el faltante en el historial de turnos para auditoría de administración.<br><br>");
        html.append("<b>P: ¿Cómo reimprimo un ticket anterior?</b><br>");
        html.append("R: Para reimprimir el último ticket inmediato, presione la tecla <b>C</b> en la pantalla de ventas. Si requiere un ticket más antiguo, diríjase al módulo de <i>Historial de Tickets</i> en el menú principal.");
        html.append("</p></td></tr></table>");
        
        // Pie de página de soporte
        html.append("<div style='margin-top: 30px; font-size: 12px; text-align: center; color: #94A3B8;'>");
        html.append("Soporte Técnico Websy Group &copy; 2026. Todos los derechos reservados.");
        html.append("</div>");
        
        html.append("</body></html>");
        
        m_editor.setText(html.toString());
        
        m_scroll = new JScrollPane(m_editor);
        m_scroll.setBorder(BorderFactory.createEmptyBorder());
        m_scroll.getViewport().setBackground(COLOR_BACKGROUND);
        
        add(m_scroll, BorderLayout.CENTER);
        
        // Panel de botones inferior
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(COLOR_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        
        JButton backBtn = new JButton("Volver al Inicio");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(COLOR_ACCENT);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(160, 36));
        
        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backBtn.setBackground(new Color(220, 175, 75));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backBtn.setBackground(COLOR_ACCENT);
            }
        });
        
        backBtn.addActionListener(event -> {
            try {
                m_App.getAppUserView().showTask("com.openbravo.pos.forms.JPanelSystemOverview");
            } catch (Exception ex) {
                // Ignore
            }
        });
        
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    @Override
    public String getTitle() {
        return "Instructivo General";
    }
    
    @Override
    public void activate() throws BasicException {
        SwingUtilities.invokeLater(() -> m_scroll.getViewport().setViewPosition(new java.awt.Point(0, 0)));
    }
    
    @Override
    public boolean deactivate() {
        return true;
    }
    
    @Override
    public JComponent getComponent() {
        return this;
    }
}

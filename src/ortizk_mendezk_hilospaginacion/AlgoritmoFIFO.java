/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ortizk_mendezk_hilospaginacion;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author karen
 */


public class AlgoritmoFIFO extends Thread {
    // Atributos para almacenar los frames y las páginas
    private ArrayList<Pagina> frames;
    private int numeroFrames;
    private ArrayList<Pagina> paginas;
    private int tiempo;
    
    // Modelo de lista para la interfaz gráfica
    private DefaultListModel<String> listModel;
    
    // Variables para control de pausa
    private volatile boolean pausado = false;
    private final Object lock = new Object();

    // Constructor
    public AlgoritmoFIFO(int numeroFrames, ArrayList<Pagina> paginas, int tiempo) {
        this.numeroFrames = numeroFrames;
        this.paginas = paginas;
        this.tiempo = tiempo;
        this.frames = new ArrayList<>(numeroFrames);
        this.listModel = new DefaultListModel<>();
    }

    @Override
    public void run() {
    int contadorFallosPagina = 0;
    int indiceReemplazo = 0;
    try {
        // Iteración sobre todas las páginas
        for (Pagina pagina : paginas) {
            // sincronización para la pausa
            synchronized (lock) {
                while (pausado) {
                    lock.wait();
                }
            }
            // Verificacion de página existente en los frames
            if (!frames.contains(pagina)) {
                contadorFallosPagina++;
                if (frames.size() >= numeroFrames) {
                    // Reemplaza página más antigua (FIFO)
                    final int finalIndiceReemplazo = indiceReemplazo;
                    frames.set(indiceReemplazo, pagina);
                    indiceReemplazo = (indiceReemplazo + 1) % numeroFrames;
                    
                    // Actualiza vista y selecciona la página reemplazada
                    SwingUtilities.invokeLater(() -> {
                        actualizarFramesVista();
                        Vista.FIFO.setSelectedIndex(finalIndiceReemplazo);
                    });
                } else {
                    // Añadir página si hay frame vacío
                    frames.add(pagina);
                    final int nuevoIndice = frames.size() - 1;
                    SwingUtilities.invokeLater(() -> {
                        actualizarFramesVista();
                        Vista.FIFO.setSelectedIndex(nuevoIndice);
                    });
                }
            } else {
                // La página ya está en los frames, actualizamos la vista sin seleccionar
                SwingUtilities.invokeLater(() -> {
                    actualizarFramesVista();
                    Vista.FIFO.clearSelection();
                });
            }
            // Actualizar contador de fallos de página en la interfaz 
            Vista.falloFIFO.setText("# Fallos de página " + contadorFallosPagina);
            // Espera antes de procesar la siguiente página
            Thread.sleep(tiempo);
        }
    } catch (InterruptedException ex) {
        Logger.getLogger(AlgoritmoFIFO.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    // Método para actualizar la vista de los frames
    public void actualizarFramesVista() {
        listModel.clear();
        for (Pagina p : frames) {
            listModel.addElement(String.valueOf(p.id));
        }
        Vista.FIFO.setModel(listModel);
        
        // Seleccionar el último elemento añadido si hay frames vacíos
        if (frames.size() < numeroFrames) {
            Vista.FIFO.setSelectedIndex(frames.size() - 1);
        }
    }

    // Método para pausar la ejecución del hilo
    public void pausar() {
        pausado = true;
    }

    // Método para reanudar la ejecución del hilo
    public void reanudar() {
        synchronized (lock) {
            pausado = false;
            lock.notify();
        }
    }
}


    


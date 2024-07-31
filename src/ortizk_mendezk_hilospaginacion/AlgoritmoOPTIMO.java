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

public class AlgoritmoOPTIMO extends Thread {
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
    public AlgoritmoOPTIMO(int numeroFrames, ArrayList<Pagina> paginas, int tiempo) {
        this.numeroFrames = numeroFrames;
        this.paginas = paginas;
        this.tiempo = tiempo;
        this.frames = new ArrayList<>(numeroFrames);
        this.listModel = new DefaultListModel<>();
    }

    @Override
    public void run() {
        int contadorFallosPagina = 0;
        int contadorPagina = 0;
        try {
            // Iteración sobre todas las páginas
            for (Pagina pagina : paginas) {
                // sincronización para la pausa
                synchronized (lock) {
                    while (pausado) {
                        lock.wait();
                    }
                }
                
                // Verifica si la página ya está en los frames
                if (!frames.contains(pagina)) {
                    contadorFallosPagina++;
                    if (frames.size() >= numeroFrames) {
                        // Encuentra la página óptima para reemplazar
                        int indexReemplazar = encontrarPaginaOptima(contadorPagina);
                        final int finalIndexReemplazar = indexReemplazar;
                        frames.set(indexReemplazar, pagina);
                        SwingUtilities.invokeLater(() -> {
                            actualizarFramesVista();
                            Vista.OPTIMO.setSelectedIndex(finalIndexReemplazar);
                        });
                    } else {
                        // Añadimos la página si hay espacio
                        frames.add(pagina);
                        final int nuevoIndice = frames.size() - 1;
                        SwingUtilities.invokeLater(() -> {
                            actualizarFramesVista();
                            Vista.OPTIMO.setSelectedIndex(nuevoIndice);
                        });
                    }
                } else {
                    /// La página ya está en los frames, actualizamos la vista sin seleccionar
                    SwingUtilities.invokeLater(() -> {
                        actualizarFramesVista();
                        Vista.OPTIMO.clearSelection();
                    });
                }
                contadorPagina++;
                // Actualiza contador de fallos de página en la interfaz
                Vista.falloOPTIMO.setText("# Fallos de página " + contadorFallosPagina);
                Thread.sleep(tiempo);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(AlgoritmoOPTIMO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    // Método para encontrar la página óptima para reemplazar
    private int encontrarPaginaOptima(int indiceActual) {
        int indexReemplazar = -1;
        int lejosDeUsar = -1;
        for (int i = 0; i < frames.size(); i++) {
            Pagina pagina = frames.get(i);
            int j;
            // Busca el próximo uso de la página actual
            for (j = indiceActual + 1; j < paginas.size(); j++) {
                if (paginas.get(j).equals(pagina)) {
                    if (j > lejosDeUsar) {
                        lejosDeUsar = j;
                        indexReemplazar = i;
                    }
                    break;
                }
            }
            // Si la página no se usa más en el futuro, se reemplaza
            if (j == paginas.size()) {
                return i;
            }
        }
        return (indexReemplazar == -1) ? 0 : indexReemplazar;
    }

    // Método para actualizar la vista de los frames
    public void actualizarFramesVista() {
        listModel.clear();
        for (Pagina p : frames) {
            listModel.addElement(String.valueOf(p.id));
        }
        Vista.OPTIMO.setModel(listModel);
        
        // Seleccionar el último elemento añadido si hay frames vacíos
        if (frames.size() < numeroFrames) {
            Vista.OPTIMO.setSelectedIndex(frames.size() - 1);
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
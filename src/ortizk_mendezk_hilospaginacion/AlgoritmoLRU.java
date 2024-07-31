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


public class AlgoritmoLRU extends Thread {
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
    public AlgoritmoLRU(int numeroFrames, ArrayList<Pagina> paginas, int tiempo) {
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
                
                // Verificam si la página ya está en los frames
                if (!frames.contains(pagina)) {
                    contadorFallosPagina++;
                    if (frames.size() >= numeroFrames) {
                        // Encuentra la página menos recientemente usada
                        int indexReemplazar = encontrarPagina(contadorPagina);
                        final int finalIndexReemplazar = indexReemplazar;
                        frames.set(indexReemplazar, pagina);
                        // Actualiza vista y selecciona la página reemplazada
                        SwingUtilities.invokeLater(() -> {
                            actualizarFramesVista();
                            Vista.LRU.setSelectedIndex(finalIndexReemplazar);
                        });
                    } else {
                        // Añadir página si hay frame vacío
                        frames.add(pagina);
                        final int nuevoIndice = frames.size() - 1;
                        SwingUtilities.invokeLater(() -> {
                            actualizarFramesVista();
                            Vista.LRU.setSelectedIndex(nuevoIndice);
                        });
                    }
                } else {
                    // La página ya está en los frames, actualizamos la vista sin seleccionar
                    SwingUtilities.invokeLater(() -> {
                        actualizarFramesVista();
                        Vista.LRU.clearSelection();
                    });
                }
                contadorPagina++;
                // Actualiza contador de fallos de página en la interfaz
                Vista.falloLRU.setText("# Fallos de página " + contadorFallosPagina);
                // Espera antes de procesar la siguiente página
                Thread.sleep(tiempo);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(AlgoritmoLRU.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    // Método para encontrar la página menos recientemente usada
    private int encontrarPagina(int indiceActual) {
        int indexReemplazar = 0;
        int usadoHaceMasTiempo = indiceActual;
        for (int i = 0; i < frames.size(); i++) {
            Pagina pagina = frames.get(i);
            int ultimoUso = -1;
            // Busca el último uso de la página actual
            for (int j = indiceActual - 1; j >= 0; j--) {
                if (paginas.get(j).equals(pagina)) {
                    ultimoUso = j;
                    break;
                }
            }
            // Actualiza si se encuentra una página usada hace más tiempo
            if (ultimoUso == -1 || ultimoUso < usadoHaceMasTiempo) {
                usadoHaceMasTiempo = ultimoUso;
                indexReemplazar = i;
            }
        }
        return indexReemplazar;
    }

    // Método para actualizar la vista de los frames
    public void actualizarFramesVista() {
        listModel.clear();
        for (Pagina p : frames) {
            listModel.addElement(String.valueOf(p.id));
        }
        Vista.LRU.setModel(listModel);
        
        // Seleccionar el último elemento añadido si hay frames vacíos
        if (frames.size() < numeroFrames) {
            Vista.LRU.setSelectedIndex(frames.size() - 1);
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
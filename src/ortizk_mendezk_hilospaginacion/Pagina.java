/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ortizk_mendezk_hilospaginacion;

import java.util.Objects;

/**
 *
 * @author karen
 */
public class Pagina {
    public int id;
    
    public Pagina(int id){
        this.id = id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pagina pagina = (Pagina) obj;
        return id == pagina.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pagina{" + "id=" + id + '}';
    }
    
    
}

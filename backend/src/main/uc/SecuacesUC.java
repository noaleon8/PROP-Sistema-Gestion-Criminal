
package main.uc;

import main.ctrl.FactoriaCtrl;
import main.ctrl.CtrlSecuaz;
import main.ctrl.CtrlCandidato;
import main.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class SecuacesUC {

    public static final int MAX_EN_OFERTA = 4;

    public Secuaz añadirSecuaz(String nombre, String apodo, TRol especialidad) {
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        Secuaz s = new Secuaz(ctrlSecuaz.siguienteId(), nombre, apodo, especialidad);
        ctrlSecuaz.insertar(s);
        return s;
    }

    public void modificarSecuaz(int idSecuaz, String nombre, String apodo){
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        Secuaz s = ctrlSecuaz.get(idSecuaz);
        s.setNombre(nombre);
        s.setApodo(apodo);
    }

    public boolean expulsar(int idSecuaz){
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        Secuaz s = ctrlSecuaz.get(idSecuaz);
        return  s.expulsar();
    }

    public boolean pagarFianza(int idSecuaz){
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        Secuaz s = ctrlSecuaz.get(idSecuaz);
        if(s.getEstado() != TEstadoSec.DETENIDO) throw new IllegalStateException("El secuaz debe estar DETENIDO para pagarle la fianza");
        boolean exito = Don.getInstance().pagar(TMovimiento.FIANZA, s.getFianza(),"Fianza de " + s.getNombre());
        if(exito){
            s.liberar();
            return true;
        }
        else return false;
    }

    public List<Secuaz> listarSecuaces(TRol especialidad){
        if(especialidad == null) throw new IllegalStateException("La especialidad no puede ser nula");
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        List<Secuaz> secuaces = ctrlSecuaz.all();
        List<Secuaz> secuacesEsp = new ArrayList<>();
        for(Secuaz s: secuaces){
            if(s.getEspecialidad() == especialidad) secuacesEsp.add(s);
        }
        return secuacesEsp;
    }

    public List<Secuaz> listarCaidos(){
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        List<Secuaz> secuaces = ctrlSecuaz.all();
        List<Secuaz> secuacesCaidos = new ArrayList<>();
        for(Secuaz s: secuaces){
            if(s.getEstado() == TEstadoSec.MUERTO) secuacesCaidos.add(s);
        }
        return secuacesCaidos;
    }

    public List<Candidato> listarOfertaCandidatos(TRol rol){
        if(rol == null) throw new IllegalStateException("El rol no puede ser nulo");
        List<Candidato> enOferta = listarEnOferta();
        List<Candidato> enOfertaRol = listarEnOferta();
        for(Candidato c: enOferta){
            if(c.getEspecialidad() == rol) enOfertaRol.add(c);
        }
        return enOfertaRol;
    }

    public List<Candidato> listarEnOferta(){
        CtrlCandidato ctrlCandidato = FactoriaCtrl.getInstance().getCtrlCandidato();
        List<Candidato> candidatos = ctrlCandidato.all();
        List<Candidato> enOferta = new ArrayList<>();
        for(Candidato c: candidatos){
            if(c.isEnOferta()) enOferta.add(c);
        }
        return enOferta;
    }

    public Secuaz fichar(String nombreCandidato){
        CtrlCandidato ctrlCandidato = FactoriaCtrl.getInstance().getCtrlCandidato();
        Candidato candidato = ctrlCandidato.get(nombreCandidato);
        if(!candidato.isEnOferta()) throw new IllegalStateException("El candidato no esta en oferta");
        boolean exito = Don.getInstance().pagar(TMovimiento.FICHAJE,candidato.getPrecioFichaje(),"Fichaje de " + nombreCandidato);
        if(!exito) return null;
        candidato.marcarFichado();
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        int id = ctrlSecuaz.siguienteId();
        Secuaz secuaz = new Secuaz(id, nombreCandidato, candidato.getApodo(), candidato.getEspecialidad());
        ctrlSecuaz.insertar(secuaz);
        return secuaz;
    }

    public List<Secuaz> listarActivos(){
        CtrlSecuaz ctrlSecuaz = FactoriaCtrl.getInstance().getCtrlSecuaz();
        List<Secuaz> secuaces = ctrlSecuaz.all();
        List<Secuaz> secuacesActivos = new ArrayList<>();
        for(Secuaz s: secuaces){
            if(s.getEstado() == TEstadoSec.LIBRE) secuacesActivos.add(s);
        }
        return secuacesActivos;
    }

    public void renovarOferta(){
        List<Candidato> candidatosOferta = listarEnOferta();
        for(Candidato c : candidatosOferta){
            c.retirarDeOferta();;
        }
        List<Candidato> candidatosElegibles = new ArrayList<>();
        CtrlCandidato ctrlCandidato = FactoriaCtrl.getInstance().getCtrlCandidato();
        List<Candidato> candidatos = ctrlCandidato.all();
        for(Candidato c :candidatos){
            if(c.esElegible()) candidatosElegibles.add(c);
        }
        Collections.shuffle(candidatosElegibles, Azar.get());
        int cantidad = Math.min(MAX_EN_OFERTA, candidatosElegibles.size());

        for (int i = 0; i < cantidad; i++) {
            candidatosElegibles.get(i).ponerEnOferta();
        }
    }


}

package todo_jaledaor.pruebatodolist.presenters;

import todo_jaledaor.pruebatodolist.dominio.CallBackInteractor;
import todo_jaledaor.pruebatodolist.dominio.ILUsuario;
import todo_jaledaor.pruebatodolist.dominio.LUsuario;
import todo_jaledaor.pruebatodolist.modelo.Usuario;
import todo_jaledaor.pruebatodolist.fragmentos.IRegistroFragmentView;

/**
 * Created by jggomez on 24-Oct-17.
 */

public class RegistroPresenter implements IRegistroPresenter {

    private IRegistroFragmentView view;
    private ILUsuario lUsuario;

    public RegistroPresenter(IRegistroFragmentView view) {
        this.view = view;
        lUsuario = new LUsuario();
    }

    @Override
    public void registrar(String nombres, String email,
                          String password) {

        view.deshabilitarControles();
        view.mostrarProgress();

        try {
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setNombres(nombres);

            lUsuario.crearUsuario(password, usuario, new CallBackInteractor<String>() {
                @Override
                public void success(String data) {
                    view.habilitarControles();
                    view.ocultarProgress();
                    view.finalizarRegistro();
                }

                @Override
                public void error(String error) {
                    view.deshabilitarControles();
                    view.ocultarProgress();
                    view.mostrarError(error);
                }
            });


        } catch (Exception e) {
            view.deshabilitarControles();
            view.ocultarProgress();
            view.mostrarError(e.getMessage());
        }

    }
}

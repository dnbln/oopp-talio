package client;

import client.scenes.AdminLoginCtrl;
import client.scenes.BoardOverviewCtrl;
import client.scenes.CardListViewCtrl;
import client.scenes.MainCtrl;
import client.scenes.ServerConnectCtrl;
import client.utils.ServerUtils;
import client.utils.ServerUtilsInterface;
import client.utils.WebsocketClientEndpoint;
import client.utils.Workspace;
import client.utils.WorkspaceProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * The module that specifies what in bound in the injector.
 */
public class MyModule extends AbstractModule {
    /**
     * Configures the module.
     */
    @Override
    protected void configure() {
        this.bind(Workspace.class).toProvider(WorkspaceProvider.class).in(Scopes.SINGLETON);
        this.bindConstant().annotatedWith(Names.named("workspaceFilename")).to("workspace");
        this.bindConstant().annotatedWith(Names.named("workspaceDir"))
                .to(".talio_workspace"); // Use a hidden directory on Unix-like systems
        this.bind(MainCtrl.class).in(Scopes.SINGLETON);
        this.bind(BoardOverviewCtrl.class).in(Scopes.SINGLETON);
        this.bind(ServerConnectCtrl.class).in(Scopes.SINGLETON);
        this.bind(AdminLoginCtrl.class).in(Scopes.SINGLETON);
        this.bind(CardListViewCtrl.class);
        this.bind(ServerUtilsInterface.class).to(ServerUtils.class).in(Scopes.SINGLETON);
        this.bind(WebsocketClientEndpoint.class);
    }
}

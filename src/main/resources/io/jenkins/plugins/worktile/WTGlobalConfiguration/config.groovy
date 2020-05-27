package io.jenkins.plugins.worktile.WTGlobalConfiguration

/* groovylint-disable-next-line CompileStatic */
f = namespace(lib.FormTagLib)
c = namespace(lib.CredentialsTagLib)

f.section(title: _('Worktile application')) {
    f.entry(title:_('Endpoint'), field:'endpoint') {
        f.textbox(default: instance.defaultEndpoint)
    }

    f.entry(title:_('Client ID'), field:'clientId') {
        f.textbox()
    }

    f.entry(title:_('Client Secret'), field:'credentialsId') {
        c.select(onchange: """{
            var self = this.targetElement ? this.targetElement : this;
            var r = findPreviousFormItem(self,'url');
            r.onchange(r);
            self = null;
            r = null;
        }""", context: app, includeUser: false, expressionAllowed: false)
    }

    f.entry(title: _('')) {
        f.validateButton(
            title: 'Test Connection',
            method: 'testConnection',
            progress: _("Testing..."),
            with: 'endpoint,clientId,credentialsId'
        )
    }
}

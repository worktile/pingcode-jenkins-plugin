package io.jenkins.plugins.worktile.WTGlobalConfiguration

/* groovylint-disable-next-line CompileStatic */
f = namespace(lib.FormTagLib)
c = namespace(lib.CredentialsTagLib)

f.section(title: _('Worktile application')) {
    f.entry(title:_('Endpoint'), field:'endpoint') {
        f.textbox(default: instance.defaultEndpoint)
    }

    f.entry(title:_('Client id'), field:'clientId') {
        f.textbox()
    }

    f.entry(title:_('Client secret'), field:'credentialsId') {
        c.select(onchange = """{
            var self = this.targetElement ? this.targetElement : this;
            var r = findPreviousFormItem(self,'url');
            r.onchange(r);
            self = null;
            r = null;
        }""" /* workaround for JENKINS-19124 */)
    }

    f.entry(title: _('')) {
        f.validateButton(title: 'TestConnection', method: 'testConnection', with: 'endpoint,clientId,credentialsId')
    }

    // f.advanced(align: 'left') {
    //     f.entry(title: _('deploy environments')) {
    //         f.repeatableHeteroProperty(field: 'envConfigs', items: instance.envConfigs, hasHeader: true) {
    //             f.repeatableDeleteButton()
    //         }
    //     }
    // }
}

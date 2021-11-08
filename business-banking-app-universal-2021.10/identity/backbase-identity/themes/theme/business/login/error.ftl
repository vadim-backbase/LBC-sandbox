<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("errorTitle")}
    <#elseif section = "form">
        <div id="kc-error-message">
            <div class="${properties.bbFormMessageClass!}">
                <span class="instruction">${message.summary}</span>
            </div>
            <#if client?? && client.baseUrl?has_content>
                <p>
                    <a id="backToApplication" href="${client.baseUrl}">
                        <i aria-hidden="true" class="bb-icon bb-icon-arrow-back bb-icon--sm align-middle"></i>
                        ${kcSanitize(msg("backToApplication"))?no_esc}
                    </a>
                </p>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>
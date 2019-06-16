<#--
Body section of the GetFeatureInfo template, it's provided with one feature collection, and
will be called multiple times if there are various fe?d?ature collections
-->
<table class="featureInfo">
  <caption class="featureInfo">Nutzungsplanung: ${type.name}</caption>
	<col style="width:30%">
	<col style="width:70%">
  <#list features as feature>
    <#assign attrs = feature.attributes >
    <tr>
      <td><strong>Typ-Bezeichnung:</strong></td>
      <td>${attrs.bezeichnung.value}</td>
    </tr>
    <tr>
      <td><strong>Kantonaler Typ:</strong></td>
      <td>${attrs.typ_kt.value}</td>
    </tr>
    <tr>
      <td><strong>Kommunaler Typ:</strong></td>
      <td>${attrs.code_kommunal.value}</td>
    </tr>
    <tr>
      <td><strong>Verbindlichkeit:</strong></td>
      <td>${attrs.verbindlichkeit.value}</td>
    </tr>
    <tr>
      <td><strong>Rechtsstatus:</strong></td>
      <td>${attrs.rechtsstatus.value}</td>
    </tr>
    <tr>
      <td><strong>Publiziert ab:</strong></td>
      <td>${attrs.publiziertab.value?date('MM/dd/yy')?string["dd. MMMM yyyy"]}</td>
    </tr>
    <tr>
      <td colspan="2"><strong>Dokumente:</strong></td>
    </tr>
    <#--
        <#if "${attrs.dokumente.value}" != "">
          <#assign documents = "${attrs.dokumente.value}"?eval>
          <#list documents as document>
              <tr>
                <td style="font-weight:500;padding-left:2em;padding-top:0em;">Titel:</td>
                <td style="padding-top:0em;">${document.titel}</td>
              </tr>
              <tr>
                <td style="font-weight:500;padding-left:2em;padding-top:0em;">Offizieller Titel:</td>
                <td style="padding-top:0em;">${document.offiziellertitel}</td>
              </tr>
              <tr>
                <td style="font-weight:500;padding-left:2em;padding-top:0em;">Nummer:</td>
                <td style="padding-top:0em;">${document.offiziellenr}</td>
              </tr>
              <tr>
                <td style="font-weight:500;padding-left:2em;padding-top:0em;">Rechtsstatus:</td>
                <td style="padding-top:0em;">${document.rechtsstatus}</td>
              </tr>
              <tr>
                <td style="font-weight:500;padding-left:2em;padding-top:0em;">Publiziert ab:</td>
                <td style="padding-top:0em;">${document.publiziertab?date('yyyy-MM-dd')?string["dd. MMMM yyyy"]}</td>
              </tr>
              <tr>
                <td style="font-weight:500;padding-left:2em;padding-top:0em;">Link:</td>
                <td style="padding-top:0em;"><a href="${document.textimweb_absolut}" target="_blank">${document.textimweb_absolut}</a></td>
              </tr>
              <tr>
                <td>&nbsp;</td>
                <td></td>
              </tr>
          </#list>
        <#else>
          &nbsp;
        </#if>
    -->
  </#list>
</table>
<br/>


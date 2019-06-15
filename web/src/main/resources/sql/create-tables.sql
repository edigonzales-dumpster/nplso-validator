/*
 * Idee: Von jedem Dokument wird das sogenannte Top-Level-Dokument eruiert.
 * Dazu bleibt aber das rekursive Abfragen der HinweisWeitere-Tabelle
 * notwendig. Kennt man von jdem Dokument das Top-Level-Dokument, kann
 * man darüber gruppieren und das JSON-Array aus den Dokumenten machen.
 */
CREATE TABLE npl_grundnutzung AS

WITH RECURSIVE x(ursprung, hinweis, parents, last_ursprung, depth) AS 
(
    SELECT 
        ursprung, 
        hinweis, 
        ARRAY[ursprung] AS parents, 
        ursprung AS last_ursprung, 
        0 AS "depth" 
    FROM 
        rechtsvorschrften_hinweisweiteredokumente
    WHERE
        ursprung != hinweis

    UNION ALL
  
    SELECT 
        x.ursprung, 
        x.hinweis, 
        parents||t1.hinweis, 
        t1.hinweis AS last_ursprung, 
        x."depth" + 1
    FROM 
        x 
        INNER JOIN rechtsvorschrften_hinweisweiteredokumente t1 
        ON (last_ursprung = t1.ursprung)
    WHERE 
        t1.hinweis IS NOT NULL
)
,
flattened_documents AS 
(
    SELECT 
        DISTINCT ON (x.last_ursprung, x.ursprung)
        --DISTINCT ON (x.last_ursprung)   
        x.ursprung AS top_level_dokument,
        x.last_ursprung AS t_id,
        dokument.t_ili_tid AS t_ili_tid,        
        dokument.titel AS titel,
        dokument.offiziellertitel AS offizellertitel,
        dokument.abkuerzung AS abkuerzung,
        dokument.offiziellenr AS offiziellenr,
        dokument.kanton AS kanton,
        dokument.gemeinde AS gemeinde,
        dokument.rechtsstatus AS rechtsstatus,
        dokument.publiziertab AS publiziertab
    FROM 
        x
        LEFT JOIN rechtsvorschrften_dokument AS dokument
        ON dokument.t_id = x.last_ursprung
    WHERE
        last_ursprung NOT IN
        (
            SELECT 
                DISTINCT ON (typ_dokument.t_id)
                typ_dokument.t_id
            FROM
                nutzungsplanung_typ_grundnutzung AS typ
                RIGHT JOIN nutzungsplanung_typ_grundnutzung_dokument AS typ_dokument
                ON typ.t_id = typ_dokument.typ_grundnutzung
        )
)
,
grouped_json_documents AS 
(
    SELECT 
       typ_grundnutzung_dokument.typ_grundnutzung,
       json_agg(json_strip_nulls(row_to_json(flattened_documents))) AS dokumente
    FROM 
        flattened_documents
        LEFT JOIN nutzungsplanung_typ_grundnutzung_dokument AS typ_grundnutzung_dokument
        ON typ_grundnutzung_dokument.dokument = flattened_documents.top_level_dokument
    WHERE 
        typ_grundnutzung IS NOT NULL
    GROUP BY
        typ_grundnutzung
        
)
SELECT
    grundnutzung.t_id AS t_id,
    grundnutzung.t_ili_tid AS t_ili_tid,
    grundnutzung.geometrie AS geometrie,
    grundnutzung.name_nummer AS name_nummer,
    grundnutzung.rechtsstatus AS rechtsstatus,
    grundnutzung.publiziertab AS publiziertab,
    grundnutzung.bemerkungen AS bemerkungen,
    grundnutzung.erfasser AS erfasser,
    grundnutzung.datum AS datum,
    typ_grundnutzung.t_id AS typ_grundnutzung_t_id, 
    typ_grundnutzung.typ_kt,
    typ_grundnutzung.code_kommunal,
    typ_grundnutzung.bezeichnung,
    typ_grundnutzung.verbindlichkeit,
    grouped_json_documents.dokumente AS dokumente
FROM
    nutzungsplanung_grundnutzung AS grundnutzung
    LEFT JOIN nutzungsplanung_typ_grundnutzung AS typ_grundnutzung
    ON typ_grundnutzung.t_id = grundnutzung.typ_grundnutzung
    LEFT JOIN grouped_json_documents
    ON grouped_json_documents.typ_grundnutzung = typ_grundnutzung.t_id
;

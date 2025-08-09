--
-- PostgreSQL database dump
--

-- Dumped from database version 14.3
-- Dumped by pg_dump version 14.3

-- Started on 2022-06-01 11:22:12

SET
    statement_timeout = 0;
SET
    lock_timeout = 0;
SET
    idle_in_transaction_session_timeout = 0;
SET
    client_encoding = 'UTF8';
SET
    standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET
    check_function_bodies = false;
SET
    xmloption = content;
SET
    client_min_messages = notice;
SET
    row_security = off;

SET SESSION AUTHORIZATION 'postgres';

ALTER SYSTEM SET max_locks_per_transaction = 16386;

SHOW max_locks_per_transaction;


--
-- TOC entry 14 (class 2615 OID 204782)
-- Name: address; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA address;


--
-- TOC entry 8 (class 2615 OID 213061)
-- Name: arrangement; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA arrangement;


--
-- TOC entry 16 (class 2615 OID 204783)
-- Name: classification; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA classification;


--
-- TOC entry 5 (class 2615 OID 204784)
-- Name: dbo; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA dbo;


--
-- TOC entry 9 (class 2615 OID 204785)
-- Name: event; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA event;


--
-- TOC entry 18 (class 2615 OID 204786)
-- Name: geography; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA geography;


--
-- TOC entry 6 (class 2615 OID 204787)
-- Name: party; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA party;


--
-- TOC entry 7 (class 2615 OID 204788)
-- Name: product; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA product;


--
-- TOC entry 10 (class 2615 OID 204789)
-- Name: resource; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA resource;


--
-- TOC entry 13 (class 2615 OID 204790)
-- Name: rules; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA rules;


--
-- TOC entry 12 (class 2615 OID 204791)
-- Name: security; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA security;


--
-- TOC entry 17 (class 2615 OID 204792)
-- Name: time; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA "time";


SET SESSION AUTHORIZATION DEFAULT;

--
-- TOC entry 2 (class 3079 OID 210600)
-- Name: tablefunc; Type: EXTENSION; Schema: -; Owner: -
--

CREATE
    EXTENSION IF NOT EXISTS tablefunc WITH SCHEMA public;


--
-- TOC entry 5614 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION tablefunc; Type: COMMENT; Schema: -; Owner:
--

COMMENT
    ON EXTENSION tablefunc IS 'functions that manipulate whole tables, including crosstab';


SET SESSION AUTHORIZATION 'postgres';

SET
    default_tablespace = '';

SET
    default_table_access_method = heap;

--
-- TOC entry 222 (class 1259 OID 204793)
-- Name: address; Type: TABLE; Schema: address; Owner: postgres
--

CREATE TABLE address.address
(
    addressid                     UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,
    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 223 (class 1259 OID 204798)
-- Name: addresssecuritytoken; Type: TABLE; Schema: address; Owner: postgres
--

CREATE TABLE address.addresssecuritytoken
(
    addresssecuritytokenid        UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    addressid                     UUID                        NOT NULL
) ;


--
-- TOC entry 224 (class 1259 OID 204803)
-- Name: addressxclassification; Type: TABLE; Schema: address; Owner: postgres
--

CREATE TABLE address.addressxclassification
(
    addressxclassificationid      UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    addressid                     UUID                        NOT NULL
) ;


--
-- TOC entry 225 (class 1259 OID 204808)
-- Name: addressxclassificationsecuritytoken; Type: TABLE; Schema: address; Owner: postgres
--

CREATE TABLE address.addressxclassificationsecuritytoken
(
    addressxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                     timestamp(6) with time zone NOT NULL,
    effectivetodate                       timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp             timestamp(6) with time zone NOT NULL,

    warehousefromdate                  DATE                        NOT NULL,

    warehouselastupdatedtimestamp         timestamp(6) with time zone NOT NULL,
    createallowed                         INTEGER                     NOT NULL,
    deleteallowed                         INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                           INTEGER                     NOT NULL,
    updateallowed                         INTEGER                     NOT NULL,
    activeflagid                          UUID                        NOT NULL,
    enterpriseid                          UUID                        NOT NULL,
    originalsourcesystemid                UUID                        NOT NULL,
    securitytokenid                       UUID                        NOT NULL,
    systemid                              UUID                        NOT NULL,
    addressxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 226 (class 1259 OID 204813)
-- Name: addressxgeography; Type: TABLE; Schema: address; Owner: postgres
--

CREATE TABLE address.addressxgeography
(
    addressxgeographyid           UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,

    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    addressid                     UUID                        NOT NULL,
    geographyid                   UUID                        NOT NULL
) ;


--
-- TOC entry 227 (class 1259 OID 204818)
-- Name: addressxgeographysecuritytoken; Type: TABLE; Schema: address; Owner: postgres
--

CREATE TABLE address.addressxgeographysecuritytoken
(
    addressxgeographysecuritytokenid UUID                        NOT NULL,
    effectivefromdate                timestamp(6) with time zone NOT NULL,
    effectivetodate                  timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp        timestamp(6) with time zone NOT NULL,

    warehousefromdate             DATE                        NOT NULL,

    warehouselastupdatedtimestamp    timestamp(6) with time zone NOT NULL,
    createallowed                    INTEGER                     NOT NULL,
    deleteallowed                    INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                      INTEGER                     NOT NULL,
    updateallowed                    INTEGER                     NOT NULL,
    activeflagid                     UUID                        NOT NULL,
    enterpriseid                     UUID                        NOT NULL,
    originalsourcesystemid           UUID                        NOT NULL,
    securitytokenid                  UUID                        NOT NULL,
    systemid                         UUID                        NOT NULL,
    addressxgeographyid              UUID                        NOT NULL
) ;


--
-- TOC entry 228 (class 1259 OID 204823)
-- Name: addressxresourceitem; Type: TABLE; Schema: address; Owner: postgres
--

CREATE TABLE address.addressxresourceitem
(
    addressxresourceitemid        UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    addressid                     UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 229 (class 1259 OID 204828)
-- Name: addressxresourceitemsecuritytoken; Type: TABLE; Schema: address; Owner: postgres
--

CREATE TABLE address.addressxresourceitemsecuritytoken
(
    addressxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                   timestamp(6) with time zone NOT NULL,
    effectivetodate                     timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp           timestamp(6) with time zone NOT NULL,
    warehousefromdate                DATE                        NOT NULL,

    warehouselastupdatedtimestamp       timestamp(6) with time zone NOT NULL,
    createallowed                       INTEGER                     NOT NULL,
    deleteallowed                       INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                         INTEGER                     NOT NULL,
    updateallowed                       INTEGER                     NOT NULL,
    activeflagid                        UUID                        NOT NULL,
    enterpriseid                        UUID                        NOT NULL,
    originalsourcesystemid              UUID                        NOT NULL,
    securitytokenid                     UUID                        NOT NULL,
    systemid                            UUID                        NOT NULL,
    addressxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 406 (class 1259 OID 213062)
-- Name: arrangement; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangement
(
    arrangementid                 UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 407 (class 1259 OID 213069)
-- Name: arrangementsecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementsecuritytoken
(
    arrangementsecuritytokenid    UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL
) ;


--
-- TOC entry 408 (class 1259 OID 213076)
-- Name: arrangementtype; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementtype
(
    arrangementtypeid             UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    arrangementtypedescription    character varying(500)      NOT NULL,
    arrangementtypename           character varying(150)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 409 (class 1259 OID 213083)
-- Name: arrangementtypesecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementtypesecuritytoken
(
    arrangementtypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate              timestamp(6) with time zone NOT NULL,
    effectivetodate                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp      timestamp(6) with time zone NOT NULL,
    warehousefromdate           DATE                        NOT NULL,

    warehouselastupdatedtimestamp  timestamp(6) with time zone NOT NULL,
    createallowed                  INTEGER                     NOT NULL,
    deleteallowed                  INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                    INTEGER                     NOT NULL,
    updateallowed                  INTEGER                     NOT NULL,
    activeflagid                   UUID                        NOT NULL,
    enterpriseid                   UUID                        NOT NULL,
    originalsourcesystemid         UUID                        NOT NULL,
    securitytokenid                UUID                        NOT NULL,
    systemid                       UUID                        NOT NULL,
    arrangementtypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 410 (class 1259 OID 213090)
-- Name: arrangementtypexclassification; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementtypexclassification
(
    arrangementtypexclassificationid UUID                        NOT NULL,
    effectivefromdate                timestamp(6) with time zone NOT NULL,
    effectivetodate                  timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp        timestamp(6) with time zone NOT NULL,
    warehousefromdate             DATE                        NOT NULL,

    warehouselastupdatedtimestamp    timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                            text                        NOT NULL,
    activeflagid                     UUID                        NOT NULL,
    enterpriseid                     UUID                        NOT NULL,
    systemid                         UUID                        NOT NULL,
    originalsourcesystemid           UUID                        NOT NULL,
    classificationid                 UUID                        NOT NULL,
    arrangementtypeid                UUID                        NOT NULL
) ;


--
-- TOC entry 411 (class 1259 OID 213097)
-- Name: arrangementtypexclassificationsecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementtypexclassificationsecuritytoken
(
    arrangementtypexclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                             timestamp(6) with time zone NOT NULL,
    effectivetodate                               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                     timestamp(6) with time zone NOT NULL,
    warehousefromdate                          DATE                        NOT NULL,

    warehouselastupdatedtimestamp                 timestamp(6) with time zone NOT NULL,
    createallowed                                 INTEGER                     NOT NULL,
    deleteallowed                                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                   INTEGER                     NOT NULL,
    updateallowed                                 INTEGER                     NOT NULL,
    activeflagid                                  UUID                        NOT NULL,
    enterpriseid                                  UUID                        NOT NULL,
    originalsourcesystemid                        UUID                        NOT NULL,
    securitytokenid                               UUID                        NOT NULL,
    systemid                                      UUID                        NOT NULL,
    arrangementtypexclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 412 (class 1259 OID 213104)
-- Name: arrangementxarrangement; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxarrangement
(
    arrangementxarrangementid     UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    childarrangementid            UUID                        NOT NULL,
    parentarrangementid           UUID                        NOT NULL
) ;


--
-- TOC entry 413 (class 1259 OID 213111)
-- Name: arrangementxarrangementsecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxarrangementsecuritytoken
(
    arrangementxarrangementsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                      timestamp(6) with time zone NOT NULL,
    effectivetodate                        timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp              timestamp(6) with time zone NOT NULL,
    warehousefromdate                   DATE                        NOT NULL,

    warehouselastupdatedtimestamp          timestamp(6) with time zone NOT NULL,
    createallowed                          INTEGER                     NOT NULL,
    deleteallowed                          INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                            INTEGER                     NOT NULL,
    updateallowed                          INTEGER                     NOT NULL,
    activeflagid                           UUID                        NOT NULL,
    enterpriseid                           UUID                        NOT NULL,
    originalsourcesystemid                 UUID                        NOT NULL,
    securitytokenid                        UUID                        NOT NULL,
    systemid                               UUID                        NOT NULL,
    arrangementxarrangementid              UUID                        NOT NULL
) ;


--
-- TOC entry 414 (class 1259 OID 213118)
-- Name: arrangementxarrangementtype; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxarrangementtype
(
    arrangementxarrangementtypeid UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL,
    arrangementtypeid             UUID                        NOT NULL
) ;


--
-- TOC entry 415 (class 1259 OID 213125)
-- Name: arrangementxarrangementtypesecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxarrangementtypesecuritytoken
(
    arrangementxarrangementtypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                          timestamp(6) with time zone NOT NULL,
    effectivetodate                            timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                  timestamp(6) with time zone NOT NULL,
    warehousefromdate                       DATE                        NOT NULL,

    warehouselastupdatedtimestamp              timestamp(6) with time zone NOT NULL,
    createallowed                              INTEGER                     NOT NULL,
    deleteallowed                              INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                INTEGER                     NOT NULL,
    updateallowed                              INTEGER                     NOT NULL,
    activeflagid                               UUID                        NOT NULL,
    enterpriseid                               UUID                        NOT NULL,
    originalsourcesystemid                     UUID                        NOT NULL,
    securitytokenid                            UUID                        NOT NULL,
    systemid                                   UUID                        NOT NULL,
    arrangementxarrangementtypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 416 (class 1259 OID 213132)
-- Name: arrangementxclassification; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxclassification
(
    arrangementxclassificationid  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL
) ;


--
-- TOC entry 417 (class 1259 OID 213139)
-- Name: arrangementxclassificationsecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxclassificationsecuritytoken
(
    arrangementxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                         timestamp(6) with time zone NOT NULL,
    effectivetodate                           timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                 timestamp(6) with time zone NOT NULL,
    warehousefromdate                      DATE                        NOT NULL,

    warehouselastupdatedtimestamp             timestamp(6) with time zone NOT NULL,
    createallowed                             INTEGER                     NOT NULL,
    deleteallowed                             INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                               INTEGER                     NOT NULL,
    updateallowed                             INTEGER                     NOT NULL,
    activeflagid                              UUID                        NOT NULL,
    enterpriseid                              UUID                        NOT NULL,
    originalsourcesystemid                    UUID                        NOT NULL,
    securitytokenid                           UUID                        NOT NULL,
    systemid                                  UUID                        NOT NULL,
    arrangementxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 418 (class 1259 OID 213146)
-- Name: arrangementxinvolvedparty; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxinvolvedparty
(
    arrangementxinvolvedpartyid   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL
) ;


--
-- TOC entry 419 (class 1259 OID 213153)
-- Name: arrangementxinvolvedpartysecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxinvolvedpartysecuritytoken
(
    arrangementxinvolvedpartysecuritytokenid UUID                        NOT NULL,
    effectivefromdate                        timestamp(6) with time zone NOT NULL,
    effectivetodate                          timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                timestamp(6) with time zone NOT NULL,
    warehousefromdate                     DATE                        NOT NULL,

    warehouselastupdatedtimestamp            timestamp(6) with time zone NOT NULL,
    createallowed                            INTEGER                     NOT NULL,
    deleteallowed                            INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                              INTEGER                     NOT NULL,
    updateallowed                            INTEGER                     NOT NULL,
    activeflagid                             UUID                        NOT NULL,
    enterpriseid                             UUID                        NOT NULL,
    originalsourcesystemid                   UUID                        NOT NULL,
    securitytokenid                          UUID                        NOT NULL,
    systemid                                 UUID                        NOT NULL,
    arrangementxinvolvedpartyid              UUID                        NOT NULL
) ;


--
-- TOC entry 420 (class 1259 OID 213160)
-- Name: arrangementxproduct; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxproduct
(
    arrangementxproductid         UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL,
    productid                     UUID                        NOT NULL
) ;


--
-- TOC entry 421 (class 1259 OID 213167)
-- Name: arrangementxproductsecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxproductsecuritytoken
(
    arrangementxproductsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                  timestamp(6) with time zone NOT NULL,
    effectivetodate                    timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp          timestamp(6) with time zone NOT NULL,
    warehousefromdate               DATE                        NOT NULL,

    warehouselastupdatedtimestamp      timestamp(6) with time zone NOT NULL,
    createallowed                      INTEGER                     NOT NULL,
    deleteallowed                      INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                        INTEGER                     NOT NULL,
    updateallowed                      INTEGER                     NOT NULL,
    activeflagid                       UUID                        NOT NULL,
    enterpriseid                       UUID                        NOT NULL,
    originalsourcesystemid             UUID                        NOT NULL,
    securitytokenid                    UUID                        NOT NULL,
    systemid                           UUID                        NOT NULL,
    arrangementxproductid              UUID                        NOT NULL
) ;


--
-- TOC entry 422 (class 1259 OID 213174)
-- Name: arrangementxresourceitem; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxresourceitem
(
    arrangementxresourceitemid    UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 423 (class 1259 OID 213181)
-- Name: arrangementxresourceitemsecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxresourceitemsecuritytoken
(
    arrangementxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                       timestamp(6) with time zone NOT NULL,
    effectivetodate                         timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp               timestamp(6) with time zone NOT NULL,
    warehousefromdate                    DATE                        NOT NULL,

    warehouselastupdatedtimestamp           timestamp(6) with time zone NOT NULL,
    createallowed                           INTEGER                     NOT NULL,
    deleteallowed                           INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                             INTEGER                     NOT NULL,
    updateallowed                           INTEGER                     NOT NULL,
    activeflagid                            UUID                        NOT NULL,
    enterpriseid                            UUID                        NOT NULL,
    originalsourcesystemid                  UUID                        NOT NULL,
    securitytokenid                         UUID                        NOT NULL,
    systemid                                UUID                        NOT NULL,
    arrangementxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 424 (class 1259 OID 213188)
-- Name: arrangementxrules; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxrules
(
    arrangementxrulesid           UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 425 (class 1259 OID 213195)
-- Name: arrangementxrulessecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxrulessecuritytoken
(
    arrangementxrulessecuritytokenid UUID                        NOT NULL,
    effectivefromdate                timestamp(6) with time zone NOT NULL,
    effectivetodate                  timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp        timestamp(6) with time zone NOT NULL,
    warehousefromdate             DATE                        NOT NULL,

    warehouselastupdatedtimestamp    timestamp(6) with time zone NOT NULL,
    createallowed                    INTEGER                     NOT NULL,
    deleteallowed                    INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                      INTEGER                     NOT NULL,
    updateallowed                    INTEGER                     NOT NULL,
    activeflagid                     UUID                        NOT NULL,
    enterpriseid                     UUID                        NOT NULL,
    originalsourcesystemid           UUID                        NOT NULL,
    securitytokenid                  UUID                        NOT NULL,
    systemid                         UUID                        NOT NULL,
    arrangementxrulesid              UUID                        NOT NULL
) ;


--
-- TOC entry 426 (class 1259 OID 213202)
-- Name: arrangementxrulestype; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxrulestype
(
    arrangementxrulestypeid       UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL,
    rulestypeid                   UUID                        NOT NULL
) ;


--
-- TOC entry 427 (class 1259 OID 213209)
-- Name: arrangementxrulestypesecuritytoken; Type: TABLE; Schema: arrangement; Owner: postgres
--

CREATE TABLE arrangement.arrangementxrulestypesecuritytoken
(
    arrangementxrulestypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                    timestamp(6) with time zone NOT NULL,
    effectivetodate                      timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp            timestamp(6) with time zone NOT NULL,
    warehousefromdate                 DATE                        NOT NULL,

    warehouselastupdatedtimestamp        timestamp(6) with time zone NOT NULL,
    createallowed                        INTEGER                     NOT NULL,
    deleteallowed                        INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                          INTEGER                     NOT NULL,
    updateallowed                        INTEGER                     NOT NULL,
    activeflagid                         UUID                        NOT NULL,
    enterpriseid                         UUID                        NOT NULL,
    originalsourcesystemid               UUID                        NOT NULL,
    securitytokenid                      UUID                        NOT NULL,
    systemid                             UUID                        NOT NULL,
    arrangementxrulestypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 230 (class 1259 OID 204833)
-- Name: classification; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classification
(
    classificationid              UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    classificationsequencenumber  integer                     NOT NULL,
    classificationdesc            character varying(500)      NOT NULL,
    classificationname            character varying(100)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationdataconceptid   UUID                        NOT NULL
) ;


--
-- TOC entry 231 (class 1259 OID 204838)
-- Name: classificationdataconcept; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationdataconcept
(
    classificationdataconceptid   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    classificationdataconceptdesc character varying(1500)     NOT NULL,
    classificationdataconceptname character varying(100)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 232 (class 1259 OID 204843)
-- Name: classificationdataconceptsecuritytoken; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationdataconceptsecuritytoken
(
    classificationdataconceptsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                        timestamp(6) with time zone NOT NULL,
    effectivetodate                          timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                timestamp(6) with time zone NOT NULL,
    warehousefromdate                     DATE                        NOT NULL,

    warehouselastupdatedtimestamp            timestamp(6) with time zone NOT NULL,
    createallowed                            INTEGER                     NOT NULL,
    deleteallowed                            INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                              INTEGER                     NOT NULL,
    updateallowed                            INTEGER                     NOT NULL,
    activeflagid                             UUID                        NOT NULL,
    enterpriseid                             UUID                        NOT NULL,
    originalsourcesystemid                   UUID                        NOT NULL,
    securitytokenid                          UUID                        NOT NULL,
    systemid                                 UUID                        NOT NULL,
    classificationdataconceptid              UUID                        NOT NULL
) ;


--
-- TOC entry 233 (class 1259 OID 204848)
-- Name: classificationdataconceptxclassification; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationdataconceptxclassification
(
    classificationdataconceptxclassificationid UUID                        NOT NULL,
    effectivefromdate                          timestamp(6) with time zone NOT NULL,
    effectivetodate                            timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                  timestamp(6) with time zone NOT NULL,

    warehousefromdate                       DATE                        NOT NULL,

    warehouselastupdatedtimestamp              timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                                      text                        NOT NULL,
    activeflagid                               UUID                        NOT NULL,
    enterpriseid                               UUID                        NOT NULL,
    systemid                                   UUID                        NOT NULL,
    originalsourcesystemid                     UUID                        NOT NULL,
    classificationid                           UUID                        NOT NULL,
    classificationdataconceptid                UUID                        NOT NULL
) ;


--
-- TOC entry 234 (class 1259 OID 204853)
-- Name: classificationdataconceptxclassificationsecuritytoken; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationdataconceptxclassificationsecuritytoken
(
    classificationdataconceptxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                                       timestamp(6) with time zone NOT NULL,
    effectivetodate                                         timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                               timestamp(6) with time zone NOT NULL,
    warehousefromdate                                    DATE                        NOT NULL,

    warehouselastupdatedtimestamp                           timestamp(6) with time zone NOT NULL,
    createallowed                                           INTEGER                     NOT NULL,
    deleteallowed                                           INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                             INTEGER                     NOT NULL,
    updateallowed                                           INTEGER                     NOT NULL,
    activeflagid                                            UUID                        NOT NULL,
    enterpriseid                                            UUID                        NOT NULL,
    originalsourcesystemid                                  UUID                        NOT NULL,
    securitytokenid                                         UUID                        NOT NULL,
    systemid                                                UUID                        NOT NULL,
    classificationdataconceptxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 235 (class 1259 OID 204858)
-- Name: classificationdataconceptxresourceitem; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationdataconceptxresourceitem
(
    classificationdataconceptxresourceitemid UUID                        NOT NULL,
    effectivefromdate                        timestamp(6) with time zone NOT NULL,
    effectivetodate                          timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                timestamp(6) with time zone NOT NULL,
    warehousefromdate                     DATE                        NOT NULL,

    warehouselastupdatedtimestamp            timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                                    text                        NOT NULL,
    activeflagid                             UUID                        NOT NULL,
    enterpriseid                             UUID                        NOT NULL,
    systemid                                 UUID                        NOT NULL,
    originalsourcesystemid                   UUID                        NOT NULL,
    classificationid                         UUID                        NOT NULL,
    classificationdataconceptid              UUID                        NOT NULL,
    resourceitemid                           UUID                        NOT NULL
) ;


--
-- TOC entry 236 (class 1259 OID 204863)
-- Name: classificationdataconceptxresourceitemsecuritytoken; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationdataconceptxresourceitemsecuritytoken
(
    classificationdataconceptxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                                     timestamp(6) with time zone NOT NULL,
    effectivetodate                                       timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                             timestamp(6) with time zone NOT NULL,
    warehousefromdate                                  DATE                        NOT NULL,

    warehouselastupdatedtimestamp                         timestamp(6) with time zone NOT NULL,
    createallowed                                         INTEGER                     NOT NULL,
    deleteallowed                                         INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                           INTEGER                     NOT NULL,
    updateallowed                                         INTEGER                     NOT NULL,
    activeflagid                                          UUID                        NOT NULL,
    enterpriseid                                          UUID                        NOT NULL,
    originalsourcesystemid                                UUID                        NOT NULL,
    securitytokenid                                       UUID                        NOT NULL,
    systemid                                              UUID                        NOT NULL,
    classificationdataconceptxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 237 (class 1259 OID 204868)
-- Name: classificationsecuritytoken; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationsecuritytoken
(
    classificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 238 (class 1259 OID 204873)
-- Name: classificationxclassification; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationxclassification
(
    classificationxclassificationid UUID                        NOT NULL,
    effectivefromdate               timestamp(6) with time zone NOT NULL,
    effectivetodate                 timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp       timestamp(6) with time zone NOT NULL,
    warehousefromdate            DATE                        NOT NULL,

    warehouselastupdatedtimestamp   timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                           text                        NOT NULL,
    activeflagid                    UUID                        NOT NULL,
    enterpriseid                    UUID                        NOT NULL,
    systemid                        UUID                        NOT NULL,
    originalsourcesystemid          UUID                        NOT NULL,
    classificationid                UUID                        NOT NULL,
    childclassificationid           UUID                        NOT NULL,
    parentclassificationid          UUID                        NOT NULL
) ;


--
-- TOC entry 239 (class 1259 OID 204878)
-- Name: classificationxclassificationsecuritytoken; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationxclassificationsecuritytoken
(
    classificationxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                            timestamp(6) with time zone NOT NULL,
    effectivetodate                              timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                    timestamp(6) with time zone NOT NULL,
    warehousefromdate                         DATE                        NOT NULL,

    warehouselastupdatedtimestamp                timestamp(6) with time zone NOT NULL,
    createallowed                                INTEGER                     NOT NULL,
    deleteallowed                                INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                  INTEGER                     NOT NULL,
    updateallowed                                INTEGER                     NOT NULL,
    activeflagid                                 UUID                        NOT NULL,
    enterpriseid                                 UUID                        NOT NULL,
    originalsourcesystemid                       UUID                        NOT NULL,
    securitytokenid                              UUID                        NOT NULL,
    systemid                                     UUID                        NOT NULL,
    classificationxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 240 (class 1259 OID 204883)
-- Name: classificationxresourceitem; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationxresourceitem
(
    classificationxresourceitemid UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 241 (class 1259 OID 204888)
-- Name: classificationxresourceitemsecuritytoken; Type: TABLE; Schema: classification; Owner: postgres
--

CREATE TABLE classification.classificationxresourceitemsecuritytoken
(
    classificationxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                          timestamp(6) with time zone NOT NULL,
    effectivetodate                            timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                  timestamp(6) with time zone NOT NULL,
    warehousefromdate                       DATE                        NOT NULL,

    warehouselastupdatedtimestamp              timestamp(6) with time zone NOT NULL,
    createallowed                              INTEGER                     NOT NULL,
    deleteallowed                              INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                INTEGER                     NOT NULL,
    updateallowed                              INTEGER                     NOT NULL,
    activeflagid                               UUID                        NOT NULL,
    enterpriseid                               UUID                        NOT NULL,
    originalsourcesystemid                     UUID                        NOT NULL,
    securitytokenid                            UUID                        NOT NULL,
    systemid                                   UUID                        NOT NULL,
    classificationxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 242 (class 1259 OID 204893)
-- Name: activeflag; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.activeflag
(
    activeflagid                  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    allowaccess                   INTEGER                     NOT NULL,
    activeflagdescription         character varying(100)      NOT NULL,
    activeflagname                character varying(100)      NOT NULL,
    enterpriseid                  UUID                        NOT NULL
) ;


--
-- TOC entry 243 (class 1259 OID 204896)
-- Name: activeflagsecuritytoken; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.activeflagsecuritytoken
(
    activeflagsecuritytokenid     UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    securitytokenactiveflagid     UUID                        NOT NULL
) ;


--
-- TOC entry 244 (class 1259 OID 204901)
-- Name: activeflagxclassification; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.activeflagxclassification
(
    activeflagxclassificationid   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 245 (class 1259 OID 204906)
-- Name: activeflagxclassificationsecuritytoken; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.activeflagxclassificationsecuritytoken
(
    activeflagxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                        timestamp(6) with time zone NOT NULL,
    effectivetodate                          timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                timestamp(6) with time zone NOT NULL,
    warehousefromdate                     DATE                        NOT NULL,

    warehouselastupdatedtimestamp            timestamp(6) with time zone NOT NULL,
    createallowed                            INTEGER                     NOT NULL,
    deleteallowed                            INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                              INTEGER                     NOT NULL,
    updateallowed                            INTEGER                     NOT NULL,
    activeflagid                             UUID                        NOT NULL,
    enterpriseid                             UUID                        NOT NULL,
    originalsourcesystemid                   UUID                        NOT NULL,
    securitytokenid                          UUID                        NOT NULL,
    systemid                                 UUID                        NOT NULL,
    activeflagxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 246 (class 1259 OID 204911)
-- Name: enterprise; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.enterprise
(
    enterpriseid                  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    enterprisedesc                character varying(255)      NOT NULL,
    enterprisename                character varying(255)      NOT NULL
) ;


--
-- TOC entry 247 (class 1259 OID 204916)
-- Name: enterprisesecuritytoken; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.enterprisesecuritytoken
(
    enterprisesecuritytokenid     UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL
) ;


--
-- TOC entry 248 (class 1259 OID 204919)
-- Name: enterprisexclassification; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.enterprisexclassification
(
    enterprisexclassificationid   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 249 (class 1259 OID 204924)
-- Name: enterprisexclassificationsecuritytoken; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.enterprisexclassificationsecuritytoken
(
    enterprisexclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                        timestamp(6) with time zone NOT NULL,
    effectivetodate                          timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                timestamp(6) with time zone NOT NULL,
    warehousefromdate                     DATE                        NOT NULL,

    warehouselastupdatedtimestamp            timestamp(6) with time zone NOT NULL,
    createallowed                            INTEGER                     NOT NULL,
    deleteallowed                            INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                              INTEGER                     NOT NULL,
    updateallowed                            INTEGER                     NOT NULL,
    activeflagid                             UUID                        NOT NULL,
    enterpriseid                             UUID                        NOT NULL,
    originalsourcesystemid                   UUID                        NOT NULL,
    securitytokenid                          UUID                        NOT NULL,
    systemid                                 UUID                        NOT NULL,
    enterprisexclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 250 (class 1259 OID 204929)
-- Name: systems; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.systems
(
    systemid                      UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    systemdesc                    character varying(250)      NOT NULL,
    systemname                    character varying(150)      NOT NULL,
    systemhistoryname             character varying(250)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL
) ;


--
-- TOC entry 251 (class 1259 OID 204934)
-- Name: systemssecuritytoken; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.systemssecuritytoken
(
    systemssecuritytokenid        UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL
) ;


--
-- TOC entry 252 (class 1259 OID 204937)
-- Name: systemxclassification; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.systemxclassification
(
    systemxclassificationid       UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 253 (class 1259 OID 204942)
-- Name: systemxclassificationsecuritytoken; Type: TABLE; Schema: dbo; Owner: postgres
--

CREATE TABLE dbo.systemxclassificationsecuritytoken
(
    systemxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                    timestamp(6) with time zone NOT NULL,
    effectivetodate                      timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp            timestamp(6) with time zone NOT NULL,
    warehousefromdate                 DATE                        NOT NULL,

    warehouselastupdatedtimestamp        timestamp(6) with time zone NOT NULL,
    createallowed                        INTEGER                     NOT NULL,
    deleteallowed                        INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                          INTEGER                     NOT NULL,
    updateallowed                        INTEGER                     NOT NULL,
    activeflagid                         UUID                        NOT NULL,
    enterpriseid                         UUID                        NOT NULL,
    originalsourcesystemid               UUID                        NOT NULL,
    securitytokenid                      UUID                        NOT NULL,
    systemid                             UUID                        NOT NULL,
    systemxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 254 (class 1259 OID 204947)
-- Name: event; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.event
(
    eventid                       UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,

    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    dayid                         INTEGER                     NOT NULL,
    hourid                        INTEGER                     NOT NULL,
    minuteid                      INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 255 (class 1259 OID 204950)
-- Name: eventsecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventsecuritytoken
(
    eventssecuritytokenid         UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    eventsid                      UUID                        NOT NULL
) ;


--
-- TOC entry 256 (class 1259 OID 204955)
-- Name: eventtype; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventtype
(
    eventtypeid                   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    eventtypedesc                 character varying(200)      NOT NULL,
    eventtypename                 character varying(200)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 257 (class 1259 OID 204960)
-- Name: eventtypessecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventtypessecuritytoken
(
    eventtypessecuritytokenid     UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    eventtypesid                  UUID                        NOT NULL
) ;


--
-- TOC entry 258 (class 1259 OID 204965)
-- Name: eventxaddress; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxaddress
(
    eventxaddressid               UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    addressid                     UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL
) ;


--
-- TOC entry 259 (class 1259 OID 204970)
-- Name: eventxaddresssecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxaddresssecuritytoken
(
    eventxaddresssecuritytokenid  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    eventxaddressid               UUID                        NOT NULL
) ;


--
-- TOC entry 260 (class 1259 OID 204975)
-- Name: eventxarrangement; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxarrangement
(
    eventxarrangementsid          UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL
) ;


--
-- TOC entry 261 (class 1259 OID 204980)
-- Name: eventxarrangementssecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxarrangementssecuritytoken
(
    eventxarrangementssecuritytokenid UUID                        NOT NULL,
    effectivefromdate                 timestamp(6) with time zone NOT NULL,
    effectivetodate                   timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp         timestamp(6) with time zone NOT NULL,
    warehousefromdate              DATE                        NOT NULL,

    warehouselastupdatedtimestamp     timestamp(6) with time zone NOT NULL,
    createallowed                     INTEGER                     NOT NULL,
    deleteallowed                     INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                       INTEGER                     NOT NULL,
    updateallowed                     INTEGER                     NOT NULL,
    activeflagid                      UUID                        NOT NULL,
    enterpriseid                      UUID                        NOT NULL,
    originalsourcesystemid            UUID                        NOT NULL,
    securitytokenid                   UUID                        NOT NULL,
    systemid                          UUID                        NOT NULL,
    eventxarrangementsid              UUID                        NOT NULL
) ;


--
-- TOC entry 262 (class 1259 OID 204985)
-- Name: eventxclassification; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxclassification
(
    eventxclassificationid        UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL
) ;


--
-- TOC entry 263 (class 1259 OID 204990)
-- Name: eventxclassificationsecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxclassificationsecuritytoken
(
    eventxclassificationssecuritytokenid UUID                        NOT NULL,
    effectivefromdate                    timestamp(6) with time zone NOT NULL,
    effectivetodate                      timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp            timestamp(6) with time zone NOT NULL,
    warehousefromdate                 DATE                        NOT NULL,

    warehouselastupdatedtimestamp        timestamp(6) with time zone NOT NULL,
    createallowed                        INTEGER                     NOT NULL,
    deleteallowed                        INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                          INTEGER                     NOT NULL,
    updateallowed                        INTEGER                     NOT NULL,
    activeflagid                         UUID                        NOT NULL,
    enterpriseid                         UUID                        NOT NULL,
    originalsourcesystemid               UUID                        NOT NULL,
    securitytokenid                      UUID                        NOT NULL,
    systemid                             UUID                        NOT NULL,
    eventxclassificationsid              UUID                        NOT NULL
) ;


--
-- TOC entry 264 (class 1259 OID 204995)
-- Name: eventxevent; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxevent
(
    eventxeventid                 UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    childeventid                  UUID                        NOT NULL,
    parenteventid                 UUID                        NOT NULL
) ;


--
-- TOC entry 265 (class 1259 OID 205000)
-- Name: eventxeventsecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxeventsecuritytoken
(
    eventxeventsecuritytokenid    UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    eventxeventid                 UUID                        NOT NULL
) ;


--
-- TOC entry 266 (class 1259 OID 205005)
-- Name: eventxeventtype; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxeventtype
(
    eventxeventtypeid             UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL,
    eventtypeid                   UUID                        NOT NULL
) ;


--
-- TOC entry 267 (class 1259 OID 205010)
-- Name: eventxeventtypesecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxeventtypesecuritytoken
(
    eventxeventtypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate              timestamp(6) with time zone NOT NULL,
    effectivetodate                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp      timestamp(6) with time zone NOT NULL,
    warehousefromdate           DATE                        NOT NULL,

    warehouselastupdatedtimestamp  timestamp(6) with time zone NOT NULL,
    createallowed                  INTEGER                     NOT NULL,
    deleteallowed                  INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                    INTEGER                     NOT NULL,
    updateallowed                  INTEGER                     NOT NULL,
    activeflagid                   UUID                        NOT NULL,
    enterpriseid                   UUID                        NOT NULL,
    originalsourcesystemid         UUID                        NOT NULL,
    securitytokenid                UUID                        NOT NULL,
    systemid                       UUID                        NOT NULL,
    eventxeventtypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 268 (class 1259 OID 205015)
-- Name: eventxgeography; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxgeography
(
    eventxgeographyid             UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL,
    geographyid                   UUID                        NOT NULL
) ;


--
-- TOC entry 269 (class 1259 OID 205020)
-- Name: eventxgeographysecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxgeographysecuritytoken
(
    eventxgeographysecuritytokenid UUID                        NOT NULL,
    effectivefromdate              timestamp(6) with time zone NOT NULL,
    effectivetodate                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp      timestamp(6) with time zone NOT NULL,
    warehousefromdate           DATE                        NOT NULL,

    warehouselastupdatedtimestamp  timestamp(6) with time zone NOT NULL,
    createallowed                  INTEGER                     NOT NULL,
    deleteallowed                  INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                    INTEGER                     NOT NULL,
    updateallowed                  INTEGER                     NOT NULL,
    activeflagid                   UUID                        NOT NULL,
    enterpriseid                   UUID                        NOT NULL,
    originalsourcesystemid         UUID                        NOT NULL,
    securitytokenid                UUID                        NOT NULL,
    systemid                       UUID                        NOT NULL,
    eventxgeographyid              UUID                        NOT NULL
) ;


--
-- TOC entry 270 (class 1259 OID 205025)
-- Name: eventxinvolvedparty; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxinvolvedparty
(
    eventxinvolvedpartyid         UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL
) ;


--
-- TOC entry 271 (class 1259 OID 205030)
-- Name: eventxinvolvedpartysecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxinvolvedpartysecuritytoken
(
    eventxinvolvedpartysecuritytokenid UUID                        NOT NULL,
    effectivefromdate                  timestamp(6) with time zone NOT NULL,
    effectivetodate                    timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp          timestamp(6) with time zone NOT NULL,
    warehousefromdate               DATE                        NOT NULL,

    warehouselastupdatedtimestamp      timestamp(6) with time zone NOT NULL,
    createallowed                      INTEGER                     NOT NULL,
    deleteallowed                      INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                        INTEGER                     NOT NULL,
    updateallowed                      INTEGER                     NOT NULL,
    activeflagid                       UUID                        NOT NULL,
    enterpriseid                       UUID                        NOT NULL,
    originalsourcesystemid             UUID                        NOT NULL,
    securitytokenid                    UUID                        NOT NULL,
    systemid                           UUID                        NOT NULL,
    eventxinvolvedpartyid              UUID                        NOT NULL
) ;


--
-- TOC entry 272 (class 1259 OID 205035)
-- Name: eventxproduct; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxproduct
(
    eventxproductid               UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL,
    productid                     UUID                        NOT NULL
) ;


--
-- TOC entry 273 (class 1259 OID 205040)
-- Name: eventxproductsecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxproductsecuritytoken
(
    eventxproductsecuritytokenid  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    eventxproductid               UUID                        NOT NULL
) ;


--
-- TOC entry 274 (class 1259 OID 205045)
-- Name: eventxresourceitem; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxresourceitem
(
    eventxresourceitemid          UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 275 (class 1259 OID 205050)
-- Name: eventxresourceitemsecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxresourceitemsecuritytoken
(
    eventxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                 timestamp(6) with time zone NOT NULL,
    effectivetodate                   timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp         timestamp(6) with time zone NOT NULL,
    warehousefromdate              DATE                        NOT NULL,

    warehouselastupdatedtimestamp     timestamp(6) with time zone NOT NULL,
    createallowed                     INTEGER                     NOT NULL,
    deleteallowed                     INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                       INTEGER                     NOT NULL,
    updateallowed                     INTEGER                     NOT NULL,
    activeflagid                      UUID                        NOT NULL,
    enterpriseid                      UUID                        NOT NULL,
    originalsourcesystemid            UUID                        NOT NULL,
    securitytokenid                   UUID                        NOT NULL,
    systemid                          UUID                        NOT NULL,
    eventxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 276 (class 1259 OID 205055)
-- Name: eventxrules; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxrules
(
    eventxrulesid                 UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    eventid                       UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 277 (class 1259 OID 205060)
-- Name: eventxrulessecuritytoken; Type: TABLE; Schema: event; Owner: postgres
--

CREATE TABLE event.eventxrulessecuritytoken
(
    eventxrulessecuritytokenid    UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    eventxrulesid                 UUID                        NOT NULL
) ;


--
-- TOC entry 278 (class 1259 OID 205065)
-- Name: geography; Type: TABLE; Schema: geography; Owner: postgres
--

CREATE TABLE geography.geography
(
    geographyid                   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    geographydesc                 character varying(500)      NOT NULL,
    geographyname                 character varying(500)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 279 (class 1259 OID 205070)
-- Name: geographysecuritytoken; Type: TABLE; Schema: geography; Owner: postgres
--

CREATE TABLE geography.geographysecuritytoken
(
    geographysecuritytokenid      UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    geographyid                   UUID                        NOT NULL
) ;


--
-- TOC entry 280 (class 1259 OID 205075)
-- Name: geographyxclassification; Type: TABLE; Schema: geography; Owner: postgres
--

CREATE TABLE geography.geographyxclassification
(
    geographyxclassificationid    UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    geographyid                   UUID                        NOT NULL
) ;


--
-- TOC entry 281 (class 1259 OID 205080)
-- Name: geographyxclassificationsecuritytoken; Type: TABLE; Schema: geography; Owner: postgres
--

CREATE TABLE geography.geographyxclassificationsecuritytoken
(
    geographyxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                       timestamp(6) with time zone NOT NULL,
    effectivetodate                         timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp               timestamp(6) with time zone NOT NULL,
    warehousefromdate                    DATE                        NOT NULL,

    warehouselastupdatedtimestamp           timestamp(6) with time zone NOT NULL,
    createallowed                           INTEGER                     NOT NULL,
    deleteallowed                           INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                             INTEGER                     NOT NULL,
    updateallowed                           INTEGER                     NOT NULL,
    activeflagid                            UUID                        NOT NULL,
    enterpriseid                            UUID                        NOT NULL,
    originalsourcesystemid                  UUID                        NOT NULL,
    securitytokenid                         UUID                        NOT NULL,
    systemid                                UUID                        NOT NULL,
    geographyxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 282 (class 1259 OID 205085)
-- Name: geographyxgeography; Type: TABLE; Schema: geography; Owner: postgres
--

CREATE TABLE geography.geographyxgeography
(
    geographyxgeographyid         UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    childgeographyid              UUID                        NOT NULL,
    parentgeographyid             UUID                        NOT NULL
) ;


--
-- TOC entry 283 (class 1259 OID 205090)
-- Name: geographyxgeographysecuritytoken; Type: TABLE; Schema: geography; Owner: postgres
--

CREATE TABLE geography.geographyxgeographysecuritytoken
(
    geographyxgeographysecuritytokenid UUID                        NOT NULL,
    effectivefromdate                  timestamp(6) with time zone NOT NULL,
    effectivetodate                    timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp          timestamp(6) with time zone NOT NULL,
    warehousefromdate               DATE                        NOT NULL,

    warehouselastupdatedtimestamp      timestamp(6) with time zone NOT NULL,
    createallowed                      INTEGER                     NOT NULL,
    deleteallowed                      INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                        INTEGER                     NOT NULL,
    updateallowed                      INTEGER                     NOT NULL,
    activeflagid                       UUID                        NOT NULL,
    enterpriseid                       UUID                        NOT NULL,
    originalsourcesystemid             UUID                        NOT NULL,
    securitytokenid                    UUID                        NOT NULL,
    systemid                           UUID                        NOT NULL,
    geographyxgeographyid              UUID                        NOT NULL
) ;


--
-- TOC entry 284 (class 1259 OID 205095)
-- Name: geographyxresourceitem; Type: TABLE; Schema: geography; Owner: postgres
--

CREATE TABLE geography.geographyxresourceitem
(
    geographyxresourceitemid      UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    geographyid                   UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 285 (class 1259 OID 205100)
-- Name: geographyxresourceitemsecuritytoken; Type: TABLE; Schema: geography; Owner: postgres
--

CREATE TABLE geography.geographyxresourceitemsecuritytoken
(
    geographyxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                     timestamp(6) with time zone NOT NULL,
    effectivetodate                       timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp             timestamp(6) with time zone NOT NULL,
    warehousefromdate                  DATE                        NOT NULL,

    warehouselastupdatedtimestamp         timestamp(6) with time zone NOT NULL,
    createallowed                         INTEGER                     NOT NULL,
    deleteallowed                         INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                           INTEGER                     NOT NULL,
    updateallowed                         INTEGER                     NOT NULL,
    activeflagid                          UUID                        NOT NULL,
    enterpriseid                          UUID                        NOT NULL,
    originalsourcesystemid                UUID                        NOT NULL,
    securitytokenid                       UUID                        NOT NULL,
    systemid                              UUID                        NOT NULL,
    geographyxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 286 (class 1259 OID 205105)
-- Name: involvedparty; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedparty
(
    involvedpartyid               UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 287 (class 1259 OID 205108)
-- Name: involvedpartyidentificationtype; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyidentificationtype
(
    involvedpartyidentificationtypeid UUID                        NOT NULL,
    effectivefromdate                 timestamp(6) with time zone NOT NULL,
    effectivetodate                   timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp         timestamp(6) with time zone NOT NULL,
    warehousefromdate              DATE                        NOT NULL,

    warehouselastupdatedtimestamp     timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    involvedpartyidentificationdesc   character varying(500)      NOT NULL,
    involvedpartyidentificationname   character varying(150)      NOT NULL,
    activeflagid                      UUID                        NOT NULL,
    enterpriseid                      UUID                        NOT NULL,
    systemid                          UUID                        NOT NULL,
    originalsourcesystemid            UUID                        NOT NULL
) ;


--
-- TOC entry 288 (class 1259 OID 205113)
-- Name: involvedpartyidentificationtypesecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyidentificationtypesecuritytoken
(
    involvedpartyidentificationtypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                              timestamp(6) with time zone NOT NULL,
    effectivetodate                                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                      timestamp(6) with time zone NOT NULL,
    warehousefromdate                           DATE                        NOT NULL,

    warehouselastupdatedtimestamp                  timestamp(6) with time zone NOT NULL,
    createallowed                                  INTEGER                     NOT NULL,
    deleteallowed                                  INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                    INTEGER                     NOT NULL,
    updateallowed                                  INTEGER                     NOT NULL,
    activeflagid                                   UUID                        NOT NULL,
    enterpriseid                                   UUID                        NOT NULL,
    originalsourcesystemid                         UUID                        NOT NULL,
    securitytokenid                                UUID                        NOT NULL,
    systemid                                       UUID                        NOT NULL,
    involvedpartyidentificationtypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 289 (class 1259 OID 205118)
-- Name: involvedpartynametype; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartynametype
(
    involvedpartynametypeid       UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    involvedpartynametypedescr    character varying(500)      NOT NULL,
    involvedpartynametypename     character varying(500)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 290 (class 1259 OID 205123)
-- Name: involvedpartynametypesecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartynametypesecuritytoken
(
    involvedpartynametypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                    timestamp(6) with time zone NOT NULL,
    effectivetodate                      timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp            timestamp(6) with time zone NOT NULL,
    warehousefromdate                 DATE                        NOT NULL,

    warehouselastupdatedtimestamp        timestamp(6) with time zone NOT NULL,
    createallowed                        INTEGER                     NOT NULL,
    deleteallowed                        INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                          INTEGER                     NOT NULL,
    updateallowed                        INTEGER                     NOT NULL,
    activeflagid                         UUID                        NOT NULL,
    enterpriseid                         UUID                        NOT NULL,
    originalsourcesystemid               UUID                        NOT NULL,
    securitytokenid                      UUID                        NOT NULL,
    systemid                             UUID                        NOT NULL,
    involvedpartynametypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 291 (class 1259 OID 205128)
-- Name: involvedpartynonorganic; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartynonorganic
(
    involvedpartynonorganicid     UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 292 (class 1259 OID 205131)
-- Name: involvedpartynonorganicsecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartynonorganicsecuritytoken
(
    involvedpartynonorganicsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                      timestamp(6) with time zone NOT NULL,
    effectivetodate                        timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp              timestamp(6) with time zone NOT NULL,
    warehousefromdate                   DATE                        NOT NULL,

    warehouselastupdatedtimestamp          timestamp(6) with time zone NOT NULL,
    createallowed                          INTEGER                     NOT NULL,
    deleteallowed                          INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                            INTEGER                     NOT NULL,
    updateallowed                          INTEGER                     NOT NULL,
    activeflagid                           UUID                        NOT NULL,
    enterpriseid                           UUID                        NOT NULL,
    originalsourcesystemid                 UUID                        NOT NULL,
    securitytokenid                        UUID                        NOT NULL,
    systemid                               UUID                        NOT NULL,
    involvedpartynonorganicid              UUID                        NOT NULL
) ;


--
-- TOC entry 293 (class 1259 OID 205136)
-- Name: involvedpartyorganic; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyorganic
(
    involvedpartyorganicid        UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 294 (class 1259 OID 205139)
-- Name: involvedpartyorganicsecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyorganicsecuritytoken
(
    involvedpartyorganicsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                   timestamp(6) with time zone NOT NULL,
    effectivetodate                     timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp           timestamp(6) with time zone NOT NULL,
    warehousefromdate                DATE                        NOT NULL,

    warehouselastupdatedtimestamp       timestamp(6) with time zone NOT NULL,
    createallowed                       INTEGER                     NOT NULL,
    deleteallowed                       INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                         INTEGER                     NOT NULL,
    updateallowed                       INTEGER                     NOT NULL,
    activeflagid                        UUID                        NOT NULL,
    enterpriseid                        UUID                        NOT NULL,
    originalsourcesystemid              UUID                        NOT NULL,
    securitytokenid                     UUID                        NOT NULL,
    systemid                            UUID                        NOT NULL,
    involvedpartyorganicid              UUID                        NOT NULL
) ;


--
-- TOC entry 295 (class 1259 OID 205144)
-- Name: involvedpartyorganictype; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyorganictype
(
    involvedpartyorganictypeid    UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    involvedpartytypedesc         character varying(500)      NOT NULL,
    involvedpartytypename         character varying(200)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 296 (class 1259 OID 205149)
-- Name: involvedpartyorganictypesecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyorganictypesecuritytoken
(
    involvedpartyorganictypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                       timestamp(6) with time zone NOT NULL,
    effectivetodate                         timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp               timestamp(6) with time zone NOT NULL,
    warehousefromdate                    DATE                        NOT NULL,

    warehouselastupdatedtimestamp           timestamp(6) with time zone NOT NULL,
    createallowed                           INTEGER                     NOT NULL,
    deleteallowed                           INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                             INTEGER                     NOT NULL,
    updateallowed                           INTEGER                     NOT NULL,
    activeflagid                            UUID                        NOT NULL,
    enterpriseid                            UUID                        NOT NULL,
    originalsourcesystemid                  UUID                        NOT NULL,
    securitytokenid                         UUID                        NOT NULL,
    systemid                                UUID                        NOT NULL,
    involvedpartyorganictypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 297 (class 1259 OID 205154)
-- Name: involvedpartysecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartysecuritytoken
(
    involvedpartysecuritytokenid  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL
) ;


--
-- TOC entry 298 (class 1259 OID 205159)
-- Name: involvedpartytype; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartytype
(
    involvedpartytypeid           UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    involvedpartytypedesc         character varying(255)      NOT NULL,
    involvedpartytypename         character varying(100)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 299 (class 1259 OID 205164)
-- Name: involvedpartytypesecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartytypesecuritytoken
(
    involvedpartytypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                timestamp(6) with time zone NOT NULL,
    effectivetodate                  timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp        timestamp(6) with time zone NOT NULL,
    warehousefromdate             DATE                        NOT NULL,

    warehouselastupdatedtimestamp    timestamp(6) with time zone NOT NULL,
    createallowed                    INTEGER                     NOT NULL,
    deleteallowed                    INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                      INTEGER                     NOT NULL,
    updateallowed                    INTEGER                     NOT NULL,
    activeflagid                     UUID                        NOT NULL,
    enterpriseid                     UUID                        NOT NULL,
    originalsourcesystemid           UUID                        NOT NULL,
    securitytokenid                  UUID                        NOT NULL,
    systemid                         UUID                        NOT NULL,
    involvedpartytypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 300 (class 1259 OID 205169)
-- Name: involvedpartyxaddress; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxaddress
(
    involvedpartyxaddressid       UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    addressid                     UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL
) ;


--
-- TOC entry 301 (class 1259 OID 205174)
-- Name: involvedpartyxaddresssecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxaddresssecuritytoken
(
    involvedpartyxaddresssecuritytokenid UUID                        NOT NULL,
    effectivefromdate                    timestamp(6) with time zone NOT NULL,
    effectivetodate                      timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp            timestamp(6) with time zone NOT NULL,
    warehousefromdate                 DATE                        NOT NULL,

    warehouselastupdatedtimestamp        timestamp(6) with time zone NOT NULL,
    createallowed                        INTEGER                     NOT NULL,
    deleteallowed                        INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                          INTEGER                     NOT NULL,
    updateallowed                        INTEGER                     NOT NULL,
    activeflagid                         UUID                        NOT NULL,
    enterpriseid                         UUID                        NOT NULL,
    originalsourcesystemid               UUID                        NOT NULL,
    securitytokenid                      UUID                        NOT NULL,
    systemid                             UUID                        NOT NULL,
    involvedpartyxaddressid              UUID                        NOT NULL
) ;


--
-- TOC entry 302 (class 1259 OID 205179)
-- Name: involvedpartyxclassification; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxclassification
(
    involvedpartyxclassificationid UUID                        NOT NULL,
    effectivefromdate              timestamp(6) with time zone NOT NULL,
    effectivetodate                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp      timestamp(6) with time zone NOT NULL,
    warehousefromdate           DATE                        NOT NULL,

    warehouselastupdatedtimestamp  timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                          text                        NOT NULL,
    activeflagid                   UUID                        NOT NULL,
    enterpriseid                   UUID                        NOT NULL,
    systemid                       UUID                        NOT NULL,
    originalsourcesystemid         UUID                        NOT NULL,
    classificationid               UUID                        NOT NULL,
    involvedpartyid                UUID                        NOT NULL
) ;


--
-- TOC entry 303 (class 1259 OID 205184)
-- Name: involvedpartyxclassificationsecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxclassificationsecuritytoken
(
    involvedpartyxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                           timestamp(6) with time zone NOT NULL,
    effectivetodate                             timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                   timestamp(6) with time zone NOT NULL,
    warehousefromdate                        DATE                        NOT NULL,

    warehouselastupdatedtimestamp               timestamp(6) with time zone NOT NULL,
    createallowed                               INTEGER                     NOT NULL,
    deleteallowed                               INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                 INTEGER                     NOT NULL,
    updateallowed                               INTEGER                     NOT NULL,
    activeflagid                                UUID                        NOT NULL,
    enterpriseid                                UUID                        NOT NULL,
    originalsourcesystemid                      UUID                        NOT NULL,
    securitytokenid                             UUID                        NOT NULL,
    systemid                                    UUID                        NOT NULL,
    involvedpartyxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 304 (class 1259 OID 205189)
-- Name: involvedpartyxinvolvedparty; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxinvolvedparty
(
    involvedpartyxinvolvedpartyid UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    childinvolvedpartyid          UUID                        NOT NULL,
    parentinvolvedpartyid         UUID                        NOT NULL
) ;


--
-- TOC entry 305 (class 1259 OID 205194)
-- Name: involvedpartyxinvolvedpartyidentificationtype; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxinvolvedpartyidentificationtype
(
    involvedpartyxinvolvedpartyidentificationtypeid UUID                        NOT NULL,
    effectivefromdate                               timestamp(6) with time zone NOT NULL,
    effectivetodate                                 timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                       timestamp(6) with time zone NOT NULL,
    warehousefromdate                            DATE                        NOT NULL,

    warehouselastupdatedtimestamp                   timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                                           text                        NOT NULL,
    activeflagid                                    UUID                        NOT NULL,
    enterpriseid                                    UUID                        NOT NULL,
    systemid                                        UUID                        NOT NULL,
    originalsourcesystemid                          UUID                        NOT NULL,
    classificationid                                UUID                        NOT NULL,
    involvedpartyid                                 UUID                        NOT NULL,
    involvedpartyidentificationtypeid               UUID                        NOT NULL
) ;


--
-- TOC entry 306 (class 1259 OID 205199)
-- Name: involvedpartyxinvolvedpartyidentificationtypesecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxinvolvedpartyidentificationtypesecuritytoken
(
    involvedpartyxinvolvedpartyidentificationtypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                                            timestamp(6) with time zone NOT NULL,
    effectivetodate                                              timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                                    timestamp(6) with time zone NOT NULL,
    warehousefromdate                                         DATE                        NOT NULL,

    warehouselastupdatedtimestamp                                timestamp(6) with time zone NOT NULL,
    createallowed                                                INTEGER                     NOT NULL,
    deleteallowed                                                INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                                  INTEGER                     NOT NULL,
    updateallowed                                                INTEGER                     NOT NULL,
    activeflagid                                                 UUID                        NOT NULL,
    enterpriseid                                                 UUID                        NOT NULL,
    originalsourcesystemid                                       UUID                        NOT NULL,
    securitytokenid                                              UUID                        NOT NULL,
    systemid                                                     UUID                        NOT NULL,
    involvedpartyxinvolvedpartyidentificationtypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 307 (class 1259 OID 205204)
-- Name: involvedpartyxinvolvedpartynametype; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxinvolvedpartynametype
(
    involvedpartyxinvolvedpartynametypeid UUID                        NOT NULL,
    effectivefromdate                     timestamp(6) with time zone NOT NULL,
    effectivetodate                       timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp             timestamp(6) with time zone NOT NULL,
    warehousefromdate                  DATE                        NOT NULL,

    warehouselastupdatedtimestamp         timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                                 text                        NOT NULL,
    activeflagid                          UUID                        NOT NULL,
    enterpriseid                          UUID                        NOT NULL,
    systemid                              UUID                        NOT NULL,
    originalsourcesystemid                UUID                        NOT NULL,
    classificationid                      UUID                        NOT NULL,
    involvedpartyid                       UUID                        NOT NULL,
    involvedpartynametypeid               UUID                        NOT NULL
) ;


--
-- TOC entry 308 (class 1259 OID 205209)
-- Name: involvedpartyxinvolvedpartynametypesecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxinvolvedpartynametypesecuritytoken
(
    involvedpartyxinvolvedpartynametypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                                  timestamp(6) with time zone NOT NULL,
    effectivetodate                                    timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                          timestamp(6) with time zone NOT NULL,
    warehousefromdate                               DATE                        NOT NULL,

    warehouselastupdatedtimestamp                      timestamp(6) with time zone NOT NULL,
    createallowed                                      INTEGER                     NOT NULL,
    deleteallowed                                      INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                        INTEGER                     NOT NULL,
    updateallowed                                      INTEGER                     NOT NULL,
    activeflagid                                       UUID                        NOT NULL,
    enterpriseid                                       UUID                        NOT NULL,
    originalsourcesystemid                             UUID                        NOT NULL,
    securitytokenid                                    UUID                        NOT NULL,
    systemid                                           UUID                        NOT NULL,
    involvedpartyxinvolvedpartynametypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 309 (class 1259 OID 205214)
-- Name: involvedpartyxinvolvedpartysecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxinvolvedpartysecuritytoken
(
    involvedpartyxinvolvedpartysecuritytokenid UUID                        NOT NULL,
    effectivefromdate                          timestamp(6) with time zone NOT NULL,
    effectivetodate                            timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                  timestamp(6) with time zone NOT NULL,
    warehousefromdate                       DATE                        NOT NULL,

    warehouselastupdatedtimestamp              timestamp(6) with time zone NOT NULL,
    createallowed                              INTEGER                     NOT NULL,
    deleteallowed                              INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                INTEGER                     NOT NULL,
    updateallowed                              INTEGER                     NOT NULL,
    activeflagid                               UUID                        NOT NULL,
    enterpriseid                               UUID                        NOT NULL,
    originalsourcesystemid                     UUID                        NOT NULL,
    securitytokenid                            UUID                        NOT NULL,
    systemid                                   UUID                        NOT NULL,
    involvedpartyxinvolvedpartyid              UUID                        NOT NULL
) ;


--
-- TOC entry 310 (class 1259 OID 205219)
-- Name: involvedpartyxinvolvedpartytype; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxinvolvedpartytype
(
    involvedpartyxinvolvedpartytypeid UUID                        NOT NULL,
    effectivefromdate                 timestamp(6) with time zone NOT NULL,
    effectivetodate                   timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp         timestamp(6) with time zone NOT NULL,
    warehousefromdate              DATE                        NOT NULL,

    warehouselastupdatedtimestamp     timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                             text                        NOT NULL,
    activeflagid                      UUID                        NOT NULL,
    enterpriseid                      UUID                        NOT NULL,
    systemid                          UUID                        NOT NULL,
    originalsourcesystemid            UUID                        NOT NULL,
    classificationid                  UUID                        NOT NULL,
    involvedpartyid                   UUID                        NOT NULL,
    involvedpartytypeid               UUID                        NOT NULL
) ;


--
-- TOC entry 311 (class 1259 OID 205224)
-- Name: involvedpartyxinvolvedpartytypesecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxinvolvedpartytypesecuritytoken
(
    involvedpartyxinvolvedpartytypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                              timestamp(6) with time zone NOT NULL,
    effectivetodate                                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                      timestamp(6) with time zone NOT NULL,
    warehousefromdate                           DATE                        NOT NULL,

    warehouselastupdatedtimestamp                  timestamp(6) with time zone NOT NULL,
    createallowed                                  INTEGER                     NOT NULL,
    deleteallowed                                  INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                    INTEGER                     NOT NULL,
    updateallowed                                  INTEGER                     NOT NULL,
    activeflagid                                   UUID                        NOT NULL,
    enterpriseid                                   UUID                        NOT NULL,
    originalsourcesystemid                         UUID                        NOT NULL,
    securitytokenid                                UUID                        NOT NULL,
    systemid                                       UUID                        NOT NULL,
    involvedpartyxinvolvedpartytypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 312 (class 1259 OID 205229)
-- Name: involvedpartyxproduct; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxproduct
(
    involvedpartyxproductid       UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL,
    productid                     UUID                        NOT NULL
) ;


--
-- TOC entry 313 (class 1259 OID 205234)
-- Name: involvedpartyxproductsecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxproductsecuritytoken
(
    involvedpartyxproductsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                    timestamp(6) with time zone NOT NULL,
    effectivetodate                      timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp            timestamp(6) with time zone NOT NULL,
    warehousefromdate                 DATE                        NOT NULL,

    warehouselastupdatedtimestamp        timestamp(6) with time zone NOT NULL,
    createallowed                        INTEGER                     NOT NULL,
    deleteallowed                        INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                          INTEGER                     NOT NULL,
    updateallowed                        INTEGER                     NOT NULL,
    activeflagid                         UUID                        NOT NULL,
    enterpriseid                         UUID                        NOT NULL,
    originalsourcesystemid               UUID                        NOT NULL,
    securitytokenid                      UUID                        NOT NULL,
    systemid                             UUID                        NOT NULL,
    involvedpartyxproductid              UUID                        NOT NULL
) ;


--
-- TOC entry 314 (class 1259 OID 205239)
-- Name: involvedpartyxproducttype; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxproducttype
(
    involvedpartyxproducttypeid   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL,
    producttypeid                 UUID                        NOT NULL
) ;


--
-- TOC entry 315 (class 1259 OID 205244)
-- Name: involvedpartyxproducttypesecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxproducttypesecuritytoken
(
    involvedpartyxproducttypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                        timestamp(6) with time zone NOT NULL,
    effectivetodate                          timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                timestamp(6) with time zone NOT NULL,
    warehousefromdate                     DATE                        NOT NULL,

    warehouselastupdatedtimestamp            timestamp(6) with time zone NOT NULL,
    createallowed                            INTEGER                     NOT NULL,
    deleteallowed                            INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                              INTEGER                     NOT NULL,
    updateallowed                            INTEGER                     NOT NULL,
    activeflagid                             UUID                        NOT NULL,
    enterpriseid                             UUID                        NOT NULL,
    originalsourcesystemid                   UUID                        NOT NULL,
    securitytokenid                          UUID                        NOT NULL,
    systemid                                 UUID                        NOT NULL,
    involvedpartyxproducttypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 316 (class 1259 OID 205249)
-- Name: involvedpartyxresourceitem; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxresourceitem
(
    involvedpartyxresourceitemid  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 317 (class 1259 OID 205254)
-- Name: involvedpartyxresourceitemsecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxresourceitemsecuritytoken
(
    involvedpartyxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                         timestamp(6) with time zone NOT NULL,
    effectivetodate                           timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                 timestamp(6) with time zone NOT NULL,
    warehousefromdate                      DATE                        NOT NULL,

    warehouselastupdatedtimestamp             timestamp(6) with time zone NOT NULL,
    createallowed                             INTEGER                     NOT NULL,
    deleteallowed                             INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                               INTEGER                     NOT NULL,
    updateallowed                             INTEGER                     NOT NULL,
    activeflagid                              UUID                        NOT NULL,
    enterpriseid                              UUID                        NOT NULL,
    originalsourcesystemid                    UUID                        NOT NULL,
    securitytokenid                           UUID                        NOT NULL,
    systemid                                  UUID                        NOT NULL,
    involvedpartyxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 318 (class 1259 OID 205259)
-- Name: involvedpartyxrules; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxrules
(
    involvedpartyxrulesid         UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 319 (class 1259 OID 205264)
-- Name: involvedpartyxrulessecuritytoken; Type: TABLE; Schema: party; Owner: postgres
--

CREATE TABLE party.involvedpartyxrulessecuritytoken
(
    involvedpartyxrulessecuritytokenid UUID                        NOT NULL,
    effectivefromdate                  timestamp(6) with time zone NOT NULL,
    effectivetodate                    timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp          timestamp(6) with time zone NOT NULL,
    warehousefromdate               DATE                        NOT NULL,

    warehouselastupdatedtimestamp      timestamp(6) with time zone NOT NULL,
    createallowed                      INTEGER                     NOT NULL,
    deleteallowed                      INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                        INTEGER                     NOT NULL,
    updateallowed                      INTEGER                     NOT NULL,
    activeflagid                       UUID                        NOT NULL,
    enterpriseid                       UUID                        NOT NULL,
    originalsourcesystemid             UUID                        NOT NULL,
    securitytokenid                    UUID                        NOT NULL,
    systemid                           UUID                        NOT NULL,
    involvedpartyxrulesid              UUID                        NOT NULL
) ;


--
-- TOC entry 320 (class 1259 OID 205269)
-- Name: product; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.product
(
    productid                     UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    productdesc                   character varying(250)      NOT NULL,
    productname                   character varying(150)      NOT NULL,
    productcode                   character varying(50)       NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 321 (class 1259 OID 205274)
-- Name: productsecuritytoken; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productsecuritytoken
(
    productsecuritytokenid        UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    productid                     UUID                        NOT NULL
) ;


--
-- TOC entry 322 (class 1259 OID 205279)
-- Name: producttype; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.producttype
(
    producttypeid                 UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    producttypedesc               character varying(200)      NOT NULL,
    producttypename               character varying(200)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 323 (class 1259 OID 205284)
-- Name: producttypessecuritytoken; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.producttypessecuritytoken
(
    producttypessecuritytokenid   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    producttypesid                UUID                        NOT NULL
) ;


--
-- TOC entry 324 (class 1259 OID 205289)
-- Name: producttypexclassification; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.producttypexclassification
(
    producttypexclassificationid  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    producttypeid                 UUID                        NOT NULL
) ;


--
-- TOC entry 325 (class 1259 OID 205294)
-- Name: producttypexclassificationsecuritytoken; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.producttypexclassificationsecuritytoken
(
    producttypexclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                         timestamp(6) with time zone NOT NULL,
    effectivetodate                           timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                 timestamp(6) with time zone NOT NULL,
    warehousefromdate                      DATE                        NOT NULL,

    warehouselastupdatedtimestamp             timestamp(6) with time zone NOT NULL,
    createallowed                             INTEGER                     NOT NULL,
    deleteallowed                             INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                               INTEGER                     NOT NULL,
    updateallowed                             INTEGER                     NOT NULL,
    activeflagid                              UUID                        NOT NULL,
    enterpriseid                              UUID                        NOT NULL,
    originalsourcesystemid                    UUID                        NOT NULL,
    securitytokenid                           UUID                        NOT NULL,
    systemid                                  UUID                        NOT NULL,
    producttypexclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 326 (class 1259 OID 205299)
-- Name: productxclassification; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productxclassification
(
    productxclassificationid      UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    productid                     UUID                        NOT NULL
) ;


--
-- TOC entry 327 (class 1259 OID 205304)
-- Name: productxclassificationsecuritytoken; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productxclassificationsecuritytoken
(
    productxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                     timestamp(6) with time zone NOT NULL,
    effectivetodate                       timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp             timestamp(6) with time zone NOT NULL,
    warehousefromdate                  DATE                        NOT NULL,

    warehouselastupdatedtimestamp         timestamp(6) with time zone NOT NULL,
    createallowed                         INTEGER                     NOT NULL,
    deleteallowed                         INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                           INTEGER                     NOT NULL,
    updateallowed                         INTEGER                     NOT NULL,
    activeflagid                          UUID                        NOT NULL,
    enterpriseid                          UUID                        NOT NULL,
    originalsourcesystemid                UUID                        NOT NULL,
    securitytokenid                       UUID                        NOT NULL,
    systemid                              UUID                        NOT NULL,
    productxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 328 (class 1259 OID 205309)
-- Name: productxproduct; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productxproduct
(
    productxproductid             UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    childproductid                UUID                        NOT NULL,
    parentproductid               UUID                        NOT NULL
) ;


--
-- TOC entry 329 (class 1259 OID 205314)
-- Name: productxproductsecuritytoken; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productxproductsecuritytoken
(
    productxproductsecuritytokenid UUID                        NOT NULL,
    effectivefromdate              timestamp(6) with time zone NOT NULL,
    effectivetodate                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp      timestamp(6) with time zone NOT NULL,
    warehousefromdate           DATE                        NOT NULL,

    warehouselastupdatedtimestamp  timestamp(6) with time zone NOT NULL,
    createallowed                  INTEGER                     NOT NULL,
    deleteallowed                  INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                    INTEGER                     NOT NULL,
    updateallowed                  INTEGER                     NOT NULL,
    activeflagid                   UUID                        NOT NULL,
    enterpriseid                   UUID                        NOT NULL,
    originalsourcesystemid         UUID                        NOT NULL,
    securitytokenid                UUID                        NOT NULL,
    systemid                       UUID                        NOT NULL,
    productxproductid              UUID                        NOT NULL
) ;


--
-- TOC entry 330 (class 1259 OID 205319)
-- Name: productxproducttype; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productxproducttype
(
    productxproducttypeid         UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    productid                     UUID                        NOT NULL,
    producttypeid                 UUID                        NOT NULL
) ;


--
-- TOC entry 331 (class 1259 OID 205324)
-- Name: productxproducttypesecuritytoken; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productxproducttypesecuritytoken
(
    productxproducttypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                  timestamp(6) with time zone NOT NULL,
    effectivetodate                    timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp          timestamp(6) with time zone NOT NULL,
    warehousefromdate               DATE                        NOT NULL,

    warehouselastupdatedtimestamp      timestamp(6) with time zone NOT NULL,
    createallowed                      INTEGER                     NOT NULL,
    deleteallowed                      INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                        INTEGER                     NOT NULL,
    updateallowed                      INTEGER                     NOT NULL,
    activeflagid                       UUID                        NOT NULL,
    enterpriseid                       UUID                        NOT NULL,
    originalsourcesystemid             UUID                        NOT NULL,
    securitytokenid                    UUID                        NOT NULL,
    systemid                           UUID                        NOT NULL,
    productxproducttypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 332 (class 1259 OID 205329)
-- Name: productxresourceitem; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productxresourceitem
(
    productxresourceitemid        UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    productid                     UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 333 (class 1259 OID 205334)
-- Name: productxresourceitemsecuritytoken; Type: TABLE; Schema: product; Owner: postgres
--

CREATE TABLE product.productxresourceitemsecuritytoken
(
    productxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                   timestamp(6) with time zone NOT NULL,
    effectivetodate                     timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp           timestamp(6) with time zone NOT NULL,
    warehousefromdate                DATE                        NOT NULL,

    warehouselastupdatedtimestamp       timestamp(6) with time zone NOT NULL,
    createallowed                       INTEGER                     NOT NULL,
    deleteallowed                       INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                         INTEGER                     NOT NULL,
    updateallowed                       INTEGER                     NOT NULL,
    activeflagid                        UUID                        NOT NULL,
    enterpriseid                        UUID                        NOT NULL,
    originalsourcesystemid              UUID                        NOT NULL,
    securitytokenid                     UUID                        NOT NULL,
    systemid                            UUID                        NOT NULL,
    productxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 334 (class 1259 OID 205339)
-- Name: arrangementhierarchyview; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.arrangementhierarchyview
(
    id       UUID NOT NULL,
    name     character varying(255),
    one      integer,
    parentid character varying(36),
    path     character varying(255),
    pather   character varying(255)
);


--
-- TOC entry 335 (class 1259 OID 205344)
-- Name: classificationhierarchyview; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.classificationhierarchyview
(
    id       UUID NOT NULL,
    name     character varying(255),
    one      integer,
    parentid character varying(36),
    path     character varying(255),
    pather   character varying(255)
);


--
-- TOC entry 336 (class 1259 OID 205349)
-- Name: geographyhierarchyview; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.geographyhierarchyview
(
    id       UUID NOT NULL,
    name     character varying(255),
    one      integer,
    parentid character varying(36),
    path     character varying(255),
    pather   character varying(255)
);


--
-- TOC entry 337 (class 1259 OID 205354)
-- Name: producthierarchyview; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.producthierarchyview
(
    id       UUID NOT NULL,
    name     character varying(255),
    one      integer,
    parentid character varying(36),
    path     character varying(255),
    pather   character varying(255)
);


--
-- TOC entry 338 (class 1259 OID 205359)
-- Name: quarters_months; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.quarters_months
(
    quarters_quarterid   integer NOT NULL,
    lumonthslist_monthid integer NOT NULL
);


--
-- TOC entry 339 (class 1259 OID 205362)
-- Name: resourceitemhierarchyview; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.resourceitemhierarchyview
(
    id       UUID NOT NULL,
    name     character varying(255),
    one      integer,
    parentid character varying(36),
    path     character varying(255),
    pather   character varying(255)
);


--
-- TOC entry 340 (class 1259 OID 205367)
-- Name: ruleshierarchyview; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ruleshierarchyview
(
    id       UUID NOT NULL,
    name     character varying(255),
    one      integer,
    parentid character varying(36),
    path     character varying(255),
    pather   character varying(255)
);


--
-- TOC entry 341 (class 1259 OID 205372)
-- Name: securityhierarchyparents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.securityhierarchyparents
(
    id    UUID NOT NULL,
    value bigint
);


--
-- TOC entry 342 (class 1259 OID 205375)
-- Name: resourceitem; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitem
(
    resourceitemid                UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    resourceitemdatatype          character varying(150)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 343 (class 1259 OID 205380)
-- Name: resourceitemdata; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemdata
(
    resourceitemdataid            UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    resourceitemdata              bytea                       NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 344 (class 1259 OID 205383)
-- Name: resourceitemdatasecuritytoken; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemdatasecuritytoken
(
    resourceitemdatasecuritytokenid UUID                        NOT NULL,
    effectivefromdate               timestamp(6) with time zone NOT NULL,
    effectivetodate                 timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp       timestamp(6) with time zone NOT NULL,
    warehousefromdate            DATE                        NOT NULL,

    warehouselastupdatedtimestamp   timestamp(6) with time zone NOT NULL,
    createallowed                   INTEGER                     NOT NULL,
    deleteallowed                   INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                     INTEGER                     NOT NULL,
    updateallowed                   INTEGER                     NOT NULL,
    activeflagid                    UUID                        NOT NULL,
    enterpriseid                    UUID                        NOT NULL,
    originalsourcesystemid          UUID                        NOT NULL,
    securitytokenid                 UUID                        NOT NULL,
    systemid                        UUID                        NOT NULL,
    resourceitemdataid              UUID                        NOT NULL
) ;


--
-- TOC entry 345 (class 1259 OID 205388)
-- Name: resourceitemdataxclassification; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemdataxclassification
(
    resourceitemdataxclassificationid UUID                        NOT NULL,
    effectivefromdate                 timestamp(6) with time zone NOT NULL,
    effectivetodate                   timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp         timestamp(6) with time zone NOT NULL,
    warehousefromdate              DATE                        NOT NULL,

    warehouselastupdatedtimestamp     timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                             text                        NOT NULL,
    activeflagid                      UUID                        NOT NULL,
    enterpriseid                      UUID                        NOT NULL,
    systemid                          UUID                        NOT NULL,
    originalsourcesystemid            UUID                        NOT NULL,
    classificationid                  UUID                        NOT NULL,
    resourceitemdataid                UUID                        NOT NULL
) ;


--
-- TOC entry 346 (class 1259 OID 205393)
-- Name: resourceitemdataxclassificationsecuritytoken; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemdataxclassificationsecuritytoken
(
    resourceitemdataxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                              timestamp(6) with time zone NOT NULL,
    effectivetodate                                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                      timestamp(6) with time zone NOT NULL,
    warehousefromdate                           DATE                        NOT NULL,

    warehouselastupdatedtimestamp                  timestamp(6) with time zone NOT NULL,
    createallowed                                  INTEGER                     NOT NULL,
    deleteallowed                                  INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                    INTEGER                     NOT NULL,
    updateallowed                                  INTEGER                     NOT NULL,
    activeflagid                                   UUID                        NOT NULL,
    enterpriseid                                   UUID                        NOT NULL,
    originalsourcesystemid                         UUID                        NOT NULL,
    securitytokenid                                UUID                        NOT NULL,
    systemid                                       UUID                        NOT NULL,
    resourceitemdataxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 347 (class 1259 OID 205398)
-- Name: resourceitemsecuritytoken; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemsecuritytoken
(
    resourceitemsecuritytokenid   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 348 (class 1259 OID 205403)
-- Name: resourceitemtype; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemtype
(
    resourceitemtypeid            UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    resourceitemtypedesc          character varying(255)      NOT NULL,
    resourceitemtypename          character varying(100)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 349 (class 1259 OID 205408)
-- Name: resourceitemtypesecuritytoken; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemtypesecuritytoken
(
    resourceitemtypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate               timestamp(6) with time zone NOT NULL,
    effectivetodate                 timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp       timestamp(6) with time zone NOT NULL,
    warehousefromdate            DATE                        NOT NULL,

    warehouselastupdatedtimestamp   timestamp(6) with time zone NOT NULL,
    createallowed                   INTEGER                     NOT NULL,
    deleteallowed                   INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                     INTEGER                     NOT NULL,
    updateallowed                   INTEGER                     NOT NULL,
    activeflagid                    UUID                        NOT NULL,
    enterpriseid                    UUID                        NOT NULL,
    originalsourcesystemid          UUID                        NOT NULL,
    securitytokenid                 UUID                        NOT NULL,
    systemid                        UUID                        NOT NULL,
    resourceitemtypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 350 (class 1259 OID 205413)
-- Name: resourceitemxclassification; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemxclassification
(
    resourceitemxclassificationid UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL
) ;


--
-- TOC entry 351 (class 1259 OID 205418)
-- Name: resourceitemxclassificationsecuritytoken; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemxclassificationsecuritytoken
(
    resourceitemxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                          timestamp(6) with time zone NOT NULL,
    effectivetodate                            timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                  timestamp(6) with time zone NOT NULL,
    warehousefromdate                       DATE                        NOT NULL,

    warehouselastupdatedtimestamp              timestamp(6) with time zone NOT NULL,
    createallowed                              INTEGER                     NOT NULL,
    deleteallowed                              INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                INTEGER                     NOT NULL,
    updateallowed                              INTEGER                     NOT NULL,
    activeflagid                               UUID                        NOT NULL,
    enterpriseid                               UUID                        NOT NULL,
    originalsourcesystemid                     UUID                        NOT NULL,
    securitytokenid                            UUID                        NOT NULL,
    systemid                                   UUID                        NOT NULL,
    resourceitemxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 352 (class 1259 OID 205423)
-- Name: resourceitemxresourceitem; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemxresourceitem
(
    resourceitemxresourceitemid   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    childresourceitemid           UUID                        NOT NULL,
    parentresourceitemid          UUID                        NOT NULL
) ;


--
-- TOC entry 353 (class 1259 OID 205428)
-- Name: resourceitemxresourceitemsecuritytoken; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemxresourceitemsecuritytoken
(
    resourceitemxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                        timestamp(6) with time zone NOT NULL,
    effectivetodate                          timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                timestamp(6) with time zone NOT NULL,
    warehousefromdate                     DATE                        NOT NULL,

    warehouselastupdatedtimestamp            timestamp(6) with time zone NOT NULL,
    createallowed                            INTEGER                     NOT NULL,
    deleteallowed                            INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                              INTEGER                     NOT NULL,
    updateallowed                            INTEGER                     NOT NULL,
    activeflagid                             UUID                        NOT NULL,
    enterpriseid                             UUID                        NOT NULL,
    originalsourcesystemid                   UUID                        NOT NULL,
    securitytokenid                          UUID                        NOT NULL,
    systemid                                 UUID                        NOT NULL,
    resourceitemxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 354 (class 1259 OID 205433)
-- Name: resourceitemxresourceitemtype; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemxresourceitemtype
(
    resourceitemxresourceitemtypeid UUID                        NOT NULL,
    effectivefromdate               timestamp(6) with time zone NOT NULL,
    effectivetodate                 timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp       timestamp(6) with time zone NOT NULL,
    warehousefromdate            DATE                        NOT NULL,

    warehouselastupdatedtimestamp   timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                           text                        NOT NULL,
    activeflagid                    UUID                        NOT NULL,
    enterpriseid                    UUID                        NOT NULL,
    systemid                        UUID                        NOT NULL,
    originalsourcesystemid          UUID                        NOT NULL,
    classificationid                UUID                        NOT NULL,
    resourceitemid                  UUID                        NOT NULL,
    resourceitemtypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 355 (class 1259 OID 205438)
-- Name: resourceitemxresourceitemtypesecuritytoken; Type: TABLE; Schema: resource; Owner: postgres
--

CREATE TABLE resource.resourceitemxresourceitemtypesecuritytoken
(
    resourceitemxresourceitemtypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate                            timestamp(6) with time zone NOT NULL,
    effectivetodate                              timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                    timestamp(6) with time zone NOT NULL,
    warehousefromdate                         DATE                        NOT NULL,

    warehouselastupdatedtimestamp                timestamp(6) with time zone NOT NULL,
    createallowed                                INTEGER                     NOT NULL,
    deleteallowed                                INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                  INTEGER                     NOT NULL,
    updateallowed                                INTEGER                     NOT NULL,
    activeflagid                                 UUID                        NOT NULL,
    enterpriseid                                 UUID                        NOT NULL,
    originalsourcesystemid                       UUID                        NOT NULL,
    securitytokenid                              UUID                        NOT NULL,
    systemid                                     UUID                        NOT NULL,
    resourceitemxresourceitemtypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 356 (class 1259 OID 205443)
-- Name: rules; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rules
(
    rulesid                       UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    rulesetdescription            character varying(250)      NOT NULL,
    rulesetname                   character varying(150)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 357 (class 1259 OID 205448)
-- Name: rulessecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulessecuritytoken
(
    rulessecuritytokenid          UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 358 (class 1259 OID 205453)
-- Name: rulestype; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulestype
(
    rulestypeid                   UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    rulestypedesc                 character varying(200)      NOT NULL,
    rulestypename                 character varying(200)      NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL
) ;


--
-- TOC entry 359 (class 1259 OID 205458)
-- Name: rulestypessecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulestypessecuritytoken
(
    rulestypessecuritytokenid     UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    rulestypesid                  UUID                        NOT NULL
) ;


--
-- TOC entry 360 (class 1259 OID 205463)
-- Name: rulestypexclassification; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulestypexclassification
(
    rulestypexclassificationid    UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    rulestypeid                   UUID                        NOT NULL
) ;


--
-- TOC entry 361 (class 1259 OID 205468)
-- Name: rulestypexclassificationsecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulestypexclassificationsecuritytoken
(
    rulestypexclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                       timestamp(6) with time zone NOT NULL,
    effectivetodate                         timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp               timestamp(6) with time zone NOT NULL,
    warehousefromdate                    DATE                        NOT NULL,

    warehouselastupdatedtimestamp           timestamp(6) with time zone NOT NULL,
    createallowed                           INTEGER                     NOT NULL,
    deleteallowed                           INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                             INTEGER                     NOT NULL,
    updateallowed                           INTEGER                     NOT NULL,
    activeflagid                            UUID                        NOT NULL,
    enterpriseid                            UUID                        NOT NULL,
    originalsourcesystemid                  UUID                        NOT NULL,
    securitytokenid                         UUID                        NOT NULL,
    systemid                                UUID                        NOT NULL,
    rulestypexclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 362 (class 1259 OID 205473)
-- Name: rulestypexresourceitem; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulestypexresourceitem
(
    rulestypexresourceitemid      UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL,
    rulestypeid                   UUID                        NOT NULL
) ;


--
-- TOC entry 363 (class 1259 OID 205478)
-- Name: rulestypexresourceitemsecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulestypexresourceitemsecuritytoken
(
    rulestypexresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                     timestamp(6) with time zone NOT NULL,
    effectivetodate                       timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp             timestamp(6) with time zone NOT NULL,
    warehousefromdate                  DATE                        NOT NULL,

    warehouselastupdatedtimestamp         timestamp(6) with time zone NOT NULL,
    createallowed                         INTEGER                     NOT NULL,
    deleteallowed                         INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                           INTEGER                     NOT NULL,
    updateallowed                         INTEGER                     NOT NULL,
    activeflagid                          UUID                        NOT NULL,
    enterpriseid                          UUID                        NOT NULL,
    originalsourcesystemid                UUID                        NOT NULL,
    securitytokenid                       UUID                        NOT NULL,
    systemid                              UUID                        NOT NULL,
    rulestypexresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 364 (class 1259 OID 205483)
-- Name: rulesxarrangement; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxarrangement
(
    rulesxarrangementsid          UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    arrangementid                 UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 365 (class 1259 OID 205488)
-- Name: rulesxarrangementssecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxarrangementssecuritytoken
(
    rulesxarrangementssecuritytokenid UUID                        NOT NULL,
    effectivefromdate                 timestamp(6) with time zone NOT NULL,
    effectivetodate                   timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp         timestamp(6) with time zone NOT NULL,
    warehousefromdate              DATE                        NOT NULL,

    warehouselastupdatedtimestamp     timestamp(6) with time zone NOT NULL,
    createallowed                     INTEGER                     NOT NULL,
    deleteallowed                     INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                       INTEGER                     NOT NULL,
    updateallowed                     INTEGER                     NOT NULL,
    activeflagid                      UUID                        NOT NULL,
    enterpriseid                      UUID                        NOT NULL,
    originalsourcesystemid            UUID                        NOT NULL,
    securitytokenid                   UUID                        NOT NULL,
    systemid                          UUID                        NOT NULL,
    rulesxarrangementsid              UUID                        NOT NULL
) ;


--
-- TOC entry 366 (class 1259 OID 205493)
-- Name: rulesxclassification; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxclassification
(
    rulesxclassificationid        UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 367 (class 1259 OID 205498)
-- Name: rulesxclassificationsecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxclassificationsecuritytoken
(
    rulesxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                   timestamp(6) with time zone NOT NULL,
    effectivetodate                     timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp           timestamp(6) with time zone NOT NULL,
    warehousefromdate                DATE                        NOT NULL,

    warehouselastupdatedtimestamp       timestamp(6) with time zone NOT NULL,
    createallowed                       INTEGER                     NOT NULL,
    deleteallowed                       INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                         INTEGER                     NOT NULL,
    updateallowed                       INTEGER                     NOT NULL,
    activeflagid                        UUID                        NOT NULL,
    enterpriseid                        UUID                        NOT NULL,
    originalsourcesystemid              UUID                        NOT NULL,
    securitytokenid                     UUID                        NOT NULL,
    systemid                            UUID                        NOT NULL,
    rulesxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 368 (class 1259 OID 205503)
-- Name: rulesxinvolvedparty; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxinvolvedparty
(
    rulesxinvolvedpartyid         UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    involvedpartyid               UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 369 (class 1259 OID 205508)
-- Name: rulesxinvolvedpartysecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxinvolvedpartysecuritytoken
(
    rulesxinvolvedpartysecuritytokenid UUID                        NOT NULL,
    effectivefromdate                  timestamp(6) with time zone NOT NULL,
    effectivetodate                    timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp          timestamp(6) with time zone NOT NULL,
    warehousefromdate               DATE                        NOT NULL,

    warehouselastupdatedtimestamp      timestamp(6) with time zone NOT NULL,
    createallowed                      INTEGER                     NOT NULL,
    deleteallowed                      INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                        INTEGER                     NOT NULL,
    updateallowed                      INTEGER                     NOT NULL,
    activeflagid                       UUID                        NOT NULL,
    enterpriseid                       UUID                        NOT NULL,
    originalsourcesystemid             UUID                        NOT NULL,
    securitytokenid                    UUID                        NOT NULL,
    systemid                           UUID                        NOT NULL,
    rulesxinvolvedpartyid              UUID                        NOT NULL
) ;


--
-- TOC entry 370 (class 1259 OID 205513)
-- Name: rulesxproduct; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxproduct
(
    rulesxproductid               UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    productid                     UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 371 (class 1259 OID 205518)
-- Name: rulesxproductsecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxproductsecuritytoken
(
    rulesxproductsecuritytokenid  UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    rulesxproductid               UUID                        NOT NULL
) ;


--
-- TOC entry 372 (class 1259 OID 205523)
-- Name: rulesxresourceitem; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxresourceitem
(
    rulesxresourceitemid          UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    resourceitemid                UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL
) ;


--
-- TOC entry 373 (class 1259 OID 205528)
-- Name: rulesxresourceitemsecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxresourceitemsecuritytoken
(
    rulesxresourceitemsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                 timestamp(6) with time zone NOT NULL,
    effectivetodate                   timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp         timestamp(6) with time zone NOT NULL,
    warehousefromdate              DATE                        NOT NULL,

    warehouselastupdatedtimestamp     timestamp(6) with time zone NOT NULL,
    createallowed                     INTEGER                     NOT NULL,
    deleteallowed                     INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                       INTEGER                     NOT NULL,
    updateallowed                     INTEGER                     NOT NULL,
    activeflagid                      UUID                        NOT NULL,
    enterpriseid                      UUID                        NOT NULL,
    originalsourcesystemid            UUID                        NOT NULL,
    securitytokenid                   UUID                        NOT NULL,
    systemid                          UUID                        NOT NULL,
    rulesxresourceitemid              UUID                        NOT NULL
) ;


--
-- TOC entry 374 (class 1259 OID 205533)
-- Name: rulesxrules; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxrules
(
    rulesxrulesid                 UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    childrulesid                  UUID                        NOT NULL,
    parentrulesid                 UUID                        NOT NULL
) ;


--
-- TOC entry 375 (class 1259 OID 205538)
-- Name: rulesxrulessecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxrulessecuritytoken
(
    rulesxrulessecuritytokenid    UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    rulesxrulesid                 UUID                        NOT NULL
) ;


--
-- TOC entry 376 (class 1259 OID 205543)
-- Name: rulesxrulestype; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxrulestype
(
    rulesxrulestypeid             UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    rulesid                       UUID                        NOT NULL,
    rulestypeid                   UUID                        NOT NULL
) ;


--
-- TOC entry 377 (class 1259 OID 205548)
-- Name: rulesxrulestypesecuritytoken; Type: TABLE; Schema: rules; Owner: postgres
--

CREATE TABLE rules.rulesxrulestypesecuritytoken
(
    rulesxrulestypesecuritytokenid UUID                        NOT NULL,
    effectivefromdate              timestamp(6) with time zone NOT NULL,
    effectivetodate                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp      timestamp(6) with time zone NOT NULL,
    warehousefromdate           DATE                        NOT NULL,

    warehouselastupdatedtimestamp  timestamp(6) with time zone NOT NULL,
    createallowed                  INTEGER                     NOT NULL,
    deleteallowed                  INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                    INTEGER                     NOT NULL,
    updateallowed                  INTEGER                     NOT NULL,
    activeflagid                   UUID                        NOT NULL,
    enterpriseid                   UUID                        NOT NULL,
    originalsourcesystemid         UUID                        NOT NULL,
    securitytokenid                UUID                        NOT NULL,
    systemid                       UUID                        NOT NULL,
    rulesxrulestypeid              UUID                        NOT NULL
) ;


--
-- TOC entry 378 (class 1259 OID 205553)
-- Name: securityhierarchy; Type: TABLE; Schema: security; Owner: postgres
--

CREATE TABLE security.securityhierarchy
(
    id       UUID NOT NULL,
    name     character varying(255),
    one      integer,
    parentid character varying(36),
    path     character varying(255),
    pather   character varying(255)
);


--
-- TOC entry 379 (class 1259 OID 205558)
-- Name: securitytoken; Type: TABLE; Schema: security; Owner: postgres
--

CREATE TABLE security.securitytoken
(
    securitytokenid                  UUID                        NOT NULL,
    effectivefromdate                timestamp(6) with time zone NOT NULL,
    effectivetodate                  timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp        timestamp(6) with time zone NOT NULL,
    warehousefromdate             DATE                        NOT NULL,

    warehouselastupdatedtimestamp    timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    securitytokenfriendlydescription character varying(255)      NOT NULL,
    securitytokenfriendlyname        character varying(255)      NOT NULL,
    securitytoken                    character varying(128)      NOT NULL,
    activeflagid                     UUID                        NOT NULL,
    enterpriseid                     UUID                        NOT NULL,
    systemid                         UUID                        NOT NULL,
    originalsourcesystemid           UUID                        NOT NULL,
    securitytokenclassificationid    UUID                        NOT NULL
) ;


--
-- TOC entry 380 (class 1259 OID 205563)
-- Name: securitytokenssecuritytoken; Type: TABLE; Schema: security; Owner: postgres
--

CREATE TABLE security.securitytokenssecuritytoken
(
    securitytokenaccessid         UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    createallowed                 INTEGER                     NOT NULL,
    deleteallowed                 INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                   INTEGER                     NOT NULL,
    updateallowed                 INTEGER                     NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    securitytokenid               UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    securitytokentoid             UUID                        NOT NULL
) ;


--
-- TOC entry 381 (class 1259 OID 205568)
-- Name: securitytokensxsecuritytokensecuritytoken; Type: TABLE; Schema: security; Owner: postgres
--

CREATE TABLE security.securitytokensxsecuritytokensecuritytoken
(
    securitytokenxsecuritytokensecuritytokenid UUID                        NOT NULL,
    effectivefromdate                          timestamp(6) with time zone NOT NULL,
    effectivetodate                            timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                  timestamp(6) with time zone NOT NULL,
    warehousefromdate                       DATE                        NOT NULL,

    warehouselastupdatedtimestamp              timestamp(6) with time zone NOT NULL,
    createallowed                              INTEGER                     NOT NULL,
    deleteallowed                              INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                INTEGER                     NOT NULL,
    updateallowed                              INTEGER                     NOT NULL,
    activeflagid                               UUID                        NOT NULL,
    enterpriseid                               UUID                        NOT NULL,
    originalsourcesystemid                     UUID                        NOT NULL,
    securitytokenid                            UUID                        NOT NULL,
    systemid                                   UUID                        NOT NULL,
    securitytokenxsecuritytokenid              UUID                        NOT NULL
) ;


--
-- TOC entry 382 (class 1259 OID 205573)
-- Name: securitytokenxclassification; Type: TABLE; Schema: security; Owner: postgres
--

CREATE TABLE security.securitytokenxclassification
(
    securitytokenxclassificationid UUID                        NOT NULL,
    effectivefromdate              timestamp(6) with time zone NOT NULL,
    effectivetodate                timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp      timestamp(6) with time zone NOT NULL,
    warehousefromdate           DATE                        NOT NULL,

    warehouselastupdatedtimestamp  timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                          text                        NOT NULL,
    activeflagid                   UUID                        NOT NULL,
    enterpriseid                   UUID                        NOT NULL,
    systemid                       UUID                        NOT NULL,
    originalsourcesystemid         UUID                        NOT NULL,
    classificationid               UUID                        NOT NULL,
    securitytokenid                UUID                        NOT NULL
) ;


--
-- TOC entry 383 (class 1259 OID 205578)
-- Name: securitytokenxclassificationsecuritytoken; Type: TABLE; Schema: security; Owner: postgres
--

CREATE TABLE security.securitytokenxclassificationsecuritytoken
(
    securitytokenxclassificationsecuritytokenid UUID                        NOT NULL,
    effectivefromdate                           timestamp(6) with time zone NOT NULL,
    effectivetodate                             timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp                   timestamp(6) with time zone NOT NULL,
    warehousefromdate                        DATE                        NOT NULL,

    warehouselastupdatedtimestamp               timestamp(6) with time zone NOT NULL,
    createallowed                               INTEGER                     NOT NULL,
    deleteallowed                               INTEGER                     NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    readallowed                                 INTEGER                     NOT NULL,
    updateallowed                               INTEGER                     NOT NULL,
    activeflagid                                UUID                        NOT NULL,
    enterpriseid                                UUID                        NOT NULL,
    originalsourcesystemid                      UUID                        NOT NULL,
    securitytokenid                             UUID                        NOT NULL,
    systemid                                    UUID                        NOT NULL,
    securitytokenxclassificationid              UUID                        NOT NULL
) ;


--
-- TOC entry 384 (class 1259 OID 205583)
-- Name: securitytokenxsecuritytoken; Type: TABLE; Schema: security; Owner: postgres
--

CREATE TABLE security.securitytokenxsecuritytoken
(
    securitytokenxsecuritytokenid UUID                        NOT NULL,
    effectivefromdate             timestamp(6) with time zone NOT NULL,
    effectivetodate               timestamp(6) with time zone NOT NULL,
    warehousecreatedtimestamp     timestamp(6) with time zone NOT NULL,
    warehousefromdate          DATE                        NOT NULL,

    warehouselastupdatedtimestamp timestamp(6) with time zone NOT NULL,
    originalsourcesystemuniqueid  UUID                        NOT NULL,
    value                         text                        NOT NULL,
    activeflagid                  UUID                        NOT NULL,
    enterpriseid                  UUID                        NOT NULL,
    systemid                      UUID                        NOT NULL,
    originalsourcesystemid        UUID                        NOT NULL,
    classificationid              UUID                        NOT NULL,
    childsecuritytokenid          UUID                        NOT NULL,
    parentsecuritytokenid         UUID                        NOT NULL
) ;


--
-- TOC entry 385 (class 1259 OID 205588)
-- Name: daynames; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".daynames
(
    daynameid                    integer                NOT NULL,
    dayabbreviation              character varying(50)  NOT NULL,
    daybusinessdayclassification character varying(50)  NOT NULL,
    dayisbusinessday             INTEGER                NOT NULL,
    daylongabbreviation          character varying(50)  NOT NULL,
    dayname                      character varying(100) NOT NULL,
    dayshortname                 character varying(200) NOT NULL,
    daysortorder                 integer                NOT NULL
);


--
-- TOC entry 386 (class 1259 OID 205591)
-- Name: dayparts; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".dayparts
(
    daypartid          integer                NOT NULL,
    daypartdescription character varying(100) NOT NULL,
    daypartname        character varying(100) NOT NULL,
    daypartsortorder   integer                NOT NULL
);


--
-- TOC entry 387 (class 1259 OID 205594)
-- Name: days; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".days
(
    dayid                        integer                     NOT NULL,
    dayddmmyyyydescription       character varying(50)       NOT NULL,
    dayddmmyyyyhyphendescription character varying(50)       NOT NULL,
    dayddmmyyyyslashdescription  character varying(50)       NOT NULL,
    daydate                      date                        NOT NULL,
    daydatetime                  timestamp(6) with time zone NOT NULL,
    dayfulldescription           character varying(50)       NOT NULL,
    dayinmonth                   integer                     NOT NULL,
    dayinyear                    integer                     NOT NULL,
    dayispublicholiday           INTEGER                     NOT NULL,
    daylongdescription           character varying(50)       NOT NULL,
    daymmqqdescription           character varying(50)       NOT NULL,
    daymonthdescription          character varying(50)       NOT NULL,
    dayyyyymmdescription         character varying(50)       NOT NULL,
    lastdayid                    integer                     NOT NULL,
    lastmonthid                  integer                     NOT NULL,
    lastquarterid                integer                     NOT NULL,
    lastyearid                   integer                     NOT NULL,
    quarterid                    integer                     NOT NULL,
    yearid                       integer                     NOT NULL,
    daynameid                    integer                     NOT NULL,
    monthid                      integer                     NOT NULL,
    weekid                       integer                     NOT NULL
);


--
-- TOC entry 388 (class 1259 OID 205597)
-- Name: halfhourdayparts; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".halfhourdayparts
(
    halfhourdaypartid integer NOT NULL,
    hourid            integer NOT NULL,
    minuteid          integer NOT NULL,
    daypartid         integer NOT NULL
);


--
-- TOC entry 389 (class 1259 OID 205600)
-- Name: halfhours; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".halfhours
(
    hourid                   integer               NOT NULL,
    minuteid                 integer               NOT NULL,
    ampmdesc                 character varying(5)  NOT NULL,
    previoushalfhourminuteid integer               NOT NULL,
    previoushourid           integer               NOT NULL,
    twelvehourclockdesc      character varying(10) NOT NULL,
    twentyfourhourclockdesc  character varying(10) NOT NULL
);


--
-- TOC entry 390 (class 1259 OID 205603)
-- Name: hours; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".hours
(
    hourid                  integer               NOT NULL,
    ampmdesc                character varying(5)  NOT NULL,
    previoushourid          integer               NOT NULL,
    twelvehourclockdesc     character varying(10) NOT NULL,
    twentyfourhourclockdesc character varying(10) NOT NULL
);


--
-- TOC entry 391 (class 1259 OID 205606)
-- Name: monthofyear; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".monthofyear
(
    monthofyearid           integer               NOT NULL,
    monthofyearabbreviation character varying(50) NOT NULL,
    monthofyearname         character varying(50) NOT NULL,
    monthofyearshortname    character varying(50) NOT NULL,
    monthinyearnumber       integer               NOT NULL
);


--
-- TOC entry 392 (class 1259 OID 205609)
-- Name: months; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".months
(
    monthid                  integer               NOT NULL,
    lastmonthid              integer               NOT NULL,
    lastquarterid            integer               NOT NULL,
    lastyearid               integer               NOT NULL,
    monthdayduration         smallint              NOT NULL,
    monthdescription         character varying(50) NOT NULL,
    monthmmmyydescription    character varying(50) NOT NULL,
    monthmmyyyydescription   character varying(50) NOT NULL,
    monthnameyyyydescription character varying(50) NOT NULL,
    monthshortdescription    character varying(50) NOT NULL,
    monthyydescription       character varying(50) NOT NULL,
    yearid                   integer               NOT NULL,
    monthofyearid            integer               NOT NULL,
    quarterid                integer               NOT NULL
);


--
-- TOC entry 393 (class 1259 OID 205612)
-- Name: publicholidays; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".publicholidays
(
    publicholidayid   integer                NOT NULL,
    dayid             integer                NOT NULL,
    publicholidayname character varying(250) NOT NULL,
    publicholidaytype character varying(250) NOT NULL
);


--
-- TOC entry 394 (class 1259 OID 205617)
-- Name: quarters; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".quarters
(
    quarterid               integer               NOT NULL,
    lastquarterid           smallint              NOT NULL,
    lastyearid              smallint              NOT NULL,
    quarterdescription      character varying(50) NOT NULL,
    quartergraphdescription character varying(50) NOT NULL,
    quartergriddescription  character varying(50) NOT NULL,
    quarterinyear           integer               NOT NULL,
    quarterqqmmdescription  character varying(50) NOT NULL,
    quartersmalldescription character varying(50) NOT NULL,
    quarteryymmdescription  character varying(50) NOT NULL,
    quarteryeardescription  character varying(50) NOT NULL,
    yearid                  smallint              NOT NULL
);


--
-- TOC entry 395 (class 1259 OID 205620)
-- Name: time; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time"."time"
(
    hourid                  integer               NOT NULL,
    minuteid                integer               NOT NULL,
    ampmdesc                character varying(5)  NOT NULL,
    previoushourid          integer               NOT NULL,
    previousminuteid        integer               NOT NULL,
    twelvehourclockdesc     character varying(10) NOT NULL,
    twentyfourhourclockdesc character varying(10) NOT NULL
);


--
-- TOC entry 396 (class 1259 OID 205623)
-- Name: trans_fiscal; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".trans_fiscal
(
    dayid       integer NOT NULL,
    fiscaldayid integer NOT NULL
);


--
-- TOC entry 397 (class 1259 OID 205626)
-- Name: trans_mtd; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".trans_mtd
(
    dayid    integer NOT NULL,
    mtddayid integer NOT NULL
);


--
-- TOC entry 398 (class 1259 OID 205629)
-- Name: trans_qtd; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".trans_qtd
(
    dayid    integer NOT NULL,
    qtddayid integer NOT NULL
);


--
-- TOC entry 399 (class 1259 OID 205632)
-- Name: trans_qtm; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".trans_qtm
(
    monthid     integer NOT NULL,
    qtm_monthid integer NOT NULL
);


--
-- TOC entry 400 (class 1259 OID 205635)
-- Name: trans_ytd; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".trans_ytd
(
    dayid    integer NOT NULL,
    ytddayid integer NOT NULL
);


--
-- TOC entry 401 (class 1259 OID 205638)
-- Name: weeks; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".weeks
(
    weekid               integer               NOT NULL,
    monthid              integer               NOT NULL,
    quarterid            integer               NOT NULL,
    weekdescription      character varying(50) NOT NULL,
    weekofmonth          integer               NOT NULL,
    weekofyear           integer               NOT NULL,
    weekshortdescription character varying(50) NOT NULL,
    yearid               integer               NOT NULL
);


--
-- TOC entry 402 (class 1259 OID 205641)
-- Name: years; Type: TABLE; Schema: time; Owner: postgres
--

CREATE TABLE "time".years
(
    yearid       smallint               NOT NULL,
    century      smallint               NOT NULL,
    lastyearid   smallint               NOT NULL,
    leapyearflag smallint               NOT NULL,
    yyname       character varying(2)   NOT NULL,
    yyyname      character varying(3)   NOT NULL,
    yearfullname character varying(250) NOT NULL,
    yearname     character varying(10)  NOT NULL
);



INSERT INTO "time".monthofyear (monthofyearid, monthofyearabbreviation, monthofyearname, monthofyearshortname,
                                monthinyearnumber)
VALUES (1, 'J', 'January', 'Jan', 0),
       (2, 'F', 'February', 'Feb', 1),
       (3, 'M', 'March', 'Mar', 2),
       (4, 'A', 'April', 'Apr', 3),
       (5, 'M', 'May', 'May', 4),
       (6, 'J', 'June', 'Jun', 5),
       (7, 'J', 'July', 'Jul', 6),
       (8, 'A', 'August', 'Aug', 7),
       (9, 'S', 'September', 'Sep', 8),
       (10, 'O', 'October', 'Oct', 9),
       (11, 'N', 'November', 'Nov', 10),
       (12, 'D', 'December', 'Dec', 11);

-- Batch insert data into the "time".dayparts table
INSERT INTO "time".dayparts (daypartid, daypartdescription, daypartname, daypartsortorder)
VALUES (2, 'Between 3.30am and 6.30 am', 'Early Morning', 2),
       (3, 'Between 6.30am and 9am', 'Morning', 3),
       (4, 'Between 9am and 10.30am', 'Late Morning', 4),
       (5, 'Between 10.30 and 12pm', 'Early Afternoon', 5),
       (6, 'Between 12pm and 2pm', 'Afternoon', 6),
       (7, 'Between 2pm and 3.30pm', 'Late Afternoon', 7),
       (8, 'Between 3.30pm and 4.30pm', 'Early Evening', 8),
       (9, 'Between 4.30pm and 7pm', 'Evening', 9),
       (10, 'Between 7pm and 9.30pm', 'Late Evening', 10),
       (11, 'Between 9.30pm and 12am', 'Midnight Evening', 11),
       (12, 'Between 12am and 3.30am', 'Midnight Morning', 1);


-- Batch insert data into the "time".daynames table
INSERT INTO "time".daynames (daynameid, dayabbreviation, daybusinessdayclassification, dayisbusinessday,
                             daylongabbreviation, dayname, dayshortname, daysortorder)
VALUES (1, 'S', 'Weekend', 0, 'Su', 'Sunday', 'Sun', 0),
       (2, 'M', 'Weekday', 1, 'Mo', 'Monday', 'Mon', 1),
       (3, 'T', 'Weekday', 1, 'Tu', 'Tuesday', 'Tue', 2),
       (4, 'W', 'Weekday', 1, 'We', 'Wednesday', 'Wed', 3),
       (5, 'T', 'Weekday', 1, 'Th', 'Thursday', 'Thur', 4),
       (6, 'F', 'Weekday', 1, 'Fr', 'Friday', 'Fri', 5),
       (7, 'S', 'Weekend', 0, 'Sa', 'Saturday', 'Sat', 6);



SET search_path TO address,arrangement,classification,"dbo","event",geography,party,product,resource,rules,"security","time";
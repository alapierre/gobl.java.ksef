package io.alapierre.ksef.fa.converter;

import org.gobl.model.Address;
import org.gobl.model.Invoice;
import org.gobl.model.Line;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.gov.crd.wzor._2023._06._29._12648.*;
import pl.gov.crd.xml.schematy.dziedzinowe.mf._2022._01._05.ed.definicjetypy.TKodKraju;

import java.util.List;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2024.01.16
 */
@Mapper
public interface InvoiceMapper {

    @Mapping(target = "fa.kodWaluty", source = "currency")
    @Mapping(target = "fa.p1", source = "issueDate")
    @Mapping(target = "fa.p2", source = "code")
    @Mapping(target = "fa.p131", source = "totals.sum")
    @Mapping(target = "podmiot1.daneIdentyfikacyjne.nazwa", source = "supplier.name")
    @Mapping(target = "podmiot1.daneIdentyfikacyjne.NIP", source = "supplier.taxId.code")
    @Mapping(target = "podmiot1.adres", source = "supplier.addresses")
    @Mapping(target = "podmiot2.daneIdentyfikacyjne.nazwa", source = "customer.name")
    @Mapping(target = "podmiot2.daneIdentyfikacyjne.NIP", source = "customer.taxId.code")
    @Mapping(target = "podmiot2.adres", source = "customer.addresses")
    @Mapping(target = "fa.faWiersz", source = "lines")
    @Mapping(target = "fa.p15", source = "totals.payable")
    Faktura invoiceToFaktura(Invoice invoice);

    Invoice fakturaToInvoice(Faktura faktura);

    default TAdres map(List<Address> value) {

        if(value.isEmpty()) return null;

        Address a = value.get(0);

        TAdres adres = new TAdres();
        if(a.getCountry() != null) adres.setKodKraju(TKodKraju.fromValue(a.getCountry()));
        adres.setAdresL1(a.getStreet() + " " + a.getNum());
        adres.setAdresL2(a.getCode() + ", " + a.getLocality());
        return adres;

    }

    @Mapping(target = "nrWierszaFa", source = "i")
    @Mapping(target = "UUID", source = "uuid")
    @Mapping(target = "p7", source = "item.name")
    @Mapping(target = "p8A", source = "item.unit")
    @Mapping(target = "p8B", source = "quantity")
    @Mapping(target = "p9A", source = "item.price")
    @Mapping(target = "p11", source = "sum")
    @Mapping(target = "p12", source = "sum")
    Faktura.Fa.FaWiersz pozycje(Line line);


    @AfterMapping
    default void handleVatRate(Line line, @MappingTarget Faktura.Fa.FaWiersz faWiersz) {
        line.getTaxes().stream().filter(tax -> "VAT".equals(tax.getCat()))
                .findFirst().ifPresent(combo -> faWiersz.setP12(combo.getPercent().replace("%", "")));
    }

    @AfterMapping
    default void fillHeader(Invoice invoice, @MappingTarget Faktura faktura) {

        TNaglowek naglowek = new TNaglowek();
        TNaglowek.KodFormularza kodFormularza = new TNaglowek.KodFormularza();
        kodFormularza.setKodSystemowy("FA (2)");
        kodFormularza.setWersjaSchemy("1-0E");
        kodFormularza.setValue(TKodFormularza.FA);

        naglowek.setKodFormularza(kodFormularza);
        naglowek.setWariantFormularza((byte) 2);

        faktura.setNaglowek(naglowek);
        determinateInvoiceType(invoice, faktura);
        determinateAdnotacja(invoice, faktura);
    }

    private static void determinateInvoiceType(Invoice invoice, Faktura faktura) {
        TRodzajFaktury typ = switch (invoice.getType()) {
            case "standard" -> TRodzajFaktury.VAT;
            case "proforma" -> throw new IllegalStateException("Proforma invoice is not supported by KEeF");
            case "corrective" -> TRodzajFaktury.KOR;
            case "credit-note" -> TRodzajFaktury.ZAL; //TODO: potwierdzić z doradcą podatkowym
            case "debit-note" -> TRodzajFaktury.ROZ; //TODO: potwierdzić z doradcą podatkowym
            default -> throw new IllegalStateException("Unexpected value: " + invoice.getType());
        };

        faktura.getFa().setRodzajFaktury(typ);
    }

    private static void determinateAdnotacja(Invoice invoice, Faktura faktura) {

        if (invoice.getTax() == null) {
            adnotacje(faktura).setP18((byte) 2);
            return;
        }

        invoice.getTax().getTags().stream().filter("reverse-charge"::equals)
                .findFirst()
                .ifPresentOrElse(
                        ignore -> adnotacje(faktura).setP18((byte) 1),
                        () -> adnotacje(faktura).setP18((byte) 2));
    }

    private static Faktura.Fa.Adnotacje adnotacje(Faktura faktura) {
        if (faktura.getFa().getAdnotacje() != null) return faktura.getFa().getAdnotacje();
        Faktura.Fa.Adnotacje a = new Faktura.Fa.Adnotacje();
        faktura.getFa().setAdnotacje(a);
        return a;

    }

}

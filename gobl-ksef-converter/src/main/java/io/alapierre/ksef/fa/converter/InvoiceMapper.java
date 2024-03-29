package io.alapierre.ksef.fa.converter;

import org.gobl.model.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.gov.crd.wzor._2023._06._29._12648.*;
import pl.gov.crd.xml.schematy.dziedzinowe.mf._2022._01._05.ed.definicjetypy.TKodKraju;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

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
    @Mapping(target = "fa.p141", source = "totals.taxes")
    @Mapping(target = "fa.platnosc.rachunekBankowy", source = "payment.instructions.creditTransfer")
    @Mapping(target = "fa.platnosc.formaPlatnosci", source = "payment.instructions")
    @Mapping(target = "fa.platnosc.terminPlatnosci", source = "payment.terms.dueDates")
    Faktura invoiceToFaktura(Invoice invoice);

    //Invoice fakturaToInvoice(Faktura faktura); // currently bidirectional conversion will not work

    @Mapping(target = "nazwaBanku", source = "name")
    @Mapping(target = "nrRB", source = "number")
    TRachunekBankowy map(CreditTransfer creditTransfer);

    default List<Faktura.Fa.Platnosc.TerminPlatnosci> mapDueDate(List<DueDate> value) {
        return value.stream()
                .map(dueDate -> {
                    Faktura.Fa.Platnosc.TerminPlatnosci terminPlatnosci = new Faktura.Fa.Platnosc.TerminPlatnosci();
                    terminPlatnosci.setTermin(convertDate(dueDate.getDate()));
                    terminPlatnosci.setTerminOpis("kwota: " + dueDate.getAmount());
                    return terminPlatnosci;
                })
                .toList();
    }

    default BigDecimal map(Total total) {
        return new BigDecimal(total.getSum());
    }

    default BigInteger map(Instructions i) {
        if (!i.getCreditTransfer().isEmpty()) return BigInteger.valueOf(6);
        else if (i.getCard() != null) return BigInteger.valueOf(2);
        return BigInteger.valueOf(1); // TODO: if do not kow - than use cache
    }

    default Optional<Integer> mapFormaPlatnosci(Instructions i) {
        if (!i.getCreditTransfer().isEmpty()) return Optional.of(6);
        else if (i.getCard() != null) return Optional.of(2);
        return Optional.empty();
    }

    default TAdres mapAdres(List<Address> value) {

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
    @Mapping(target = "p12", source = "taxes")
    Faktura.Fa.FaWiersz pozycje(Line line);

    default String map(List<Combo> taxes) {
        return taxes.stream()
                .filter(combo -> "VAT".equals(combo.getCat()))
                .findFirst()
                .map(combo -> combo.getPercent() != null ? combo.getPercent().replace(".0%", "") : null)
                .orElse(null);
    }

//    @AfterMapping
//    default void handleVatRate(Line line, @MappingTarget Faktura.Fa.FaWiersz faWiersz) {
//        line.getTaxes().stream().filter(tax -> "VAT".equals(tax.getCat()))
//                .findFirst().ifPresent(combo -> faWiersz.setP12(combo.getPercent().replace(".0%", "")));
//    }

    @AfterMapping
    default void fillHeader(Invoice invoice, @MappingTarget Faktura faktura) {

        TNaglowek naglowek = new TNaglowek();
        TNaglowek.KodFormularza kodFormularza = new TNaglowek.KodFormularza();
        kodFormularza.setKodSystemowy("FA (2)");
        kodFormularza.setWersjaSchemy("1-0E");
        kodFormularza.setValue(TKodFormularza.FA);

        naglowek.setKodFormularza(kodFormularza);
        naglowek.setWariantFormularza((byte) 2);
        try {
            naglowek.setDataWytworzeniaFa( DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
        } catch (DatatypeConfigurationException ignore) {}
        naglowek.setSystemInfo("GOBL Java FA(2) Converter");

        faktura.setNaglowek(naglowek);
        determinateInvoiceType(invoice, faktura);
        determinateAdnotacje(invoice, faktura);
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

    private static void determinateAdnotacje(Invoice invoice, Faktura faktura) {

        Faktura.Fa.Adnotacje adn = adnotacje(faktura);
        adn.setP16((byte) 2);
        adn.setP17((byte) 2);
        adn.setP18A((byte) 2);
        adn.setP23((byte) 2);
        Faktura.Fa.Adnotacje.Zwolnienie zwolnienie = new Faktura.Fa.Adnotacje.Zwolnienie();
        zwolnienie.setP19N((byte) 1);
        adn.setZwolnienie(zwolnienie);
        Faktura.Fa.Adnotacje.NoweSrodkiTransportu noweSrodkiTransportu = new Faktura.Fa.Adnotacje.NoweSrodkiTransportu();
        noweSrodkiTransportu.setP22N((byte) 1);
        adn.setNoweSrodkiTransportu(noweSrodkiTransportu);

        Faktura.Fa.Adnotacje.PMarzy pmarzy = new Faktura.Fa.Adnotacje.PMarzy();
        pmarzy.setPPMarzyN((byte) 1);
        adn.setPMarzy(pmarzy);

        if (invoice.getTax() == null) {
            adn.setP18((byte) 2);
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

    private static XMLGregorianCalendar convertDate(String date) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
        } catch (DatatypeConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

}

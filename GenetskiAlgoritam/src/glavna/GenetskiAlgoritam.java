package glavna;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

//jedinka koja je najbolja nece mutirati ni nestati ali moze uci u krizanje.
//krizanje stvara dvoje djece od odabranih roditelja, ako se ne krizaju roditelji se prepisuju dalje
public class GenetskiAlgoritam {

    public static void main(String[] args) {
        Integer velicinaPopulacije = null, brojIteracija = null;
        Double vjerojatnostKrizanja = null, vjerojatnostMutacije = null;
        BufferedReader citac;
        // Ucitavamo parametre algoritma iz vanjskog file-a
        try {
            citac = new BufferedReader(new FileReader(new File("parametri.txt")));
            velicinaPopulacije = Integer.parseInt(citac.readLine());
            vjerojatnostKrizanja = Double.parseDouble(citac.readLine());
            vjerojatnostMutacije = Double.parseDouble(citac.readLine());
            brojIteracija = Integer.parseInt(citac.readLine());
            citac.close();
        } catch (FileNotFoundException e) {
            System.out.println("Nema file-a");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Greska s pisanjem");
            e.printStackTrace();
        }

        // stvorimo populaciju
        ArrayList<Integer> populacija = stvoriPopulaciju(velicinaPopulacije);

        for (int j = 0; j < brojIteracija; j++) {

            System.out.println("Broj iteracije je " + (j+1));

            populacija = selekcija(populacija);
            int indexNajboljeJedinke = nadiNajboljuJedinku(populacija);
            populacija = krizanje(populacija, vjerojatnostKrizanja,velicinaPopulacije, vjerojatnostMutacije);
            populacija = mutacija(populacija, vjerojatnostMutacije);
            Integer max = 0;
            double ukupnaDobrota=0.0;

            for (Integer jedinka : populacija) {
                ukupnaDobrota+=dobrota(jedinka);
                if (dobrota(jedinka) > dobrota(max)) {
                    max = jedinka;
                }
            }

            double prosjecnaDobrota=ukupnaDobrota/populacija.size();

            System.out.print("Normirana dobrota svake jedinke: ");
            for (int i = 0; i < populacija.size(); i++)
            {
                System.out.print(dobrota(populacija.get(i)) / prosjecnaDobrota);
                if(i!=populacija.size()-1) System.out.print(", ");
            }
            System.out.println("");

            System.out.println("Trenutna maximalna vrijednost dobrote: " + dobrota(max) + " za jedinku s vrijednosti " + max);
            System.out.println("Prosječna vrijednost dobrote u populaciji " + prosjecnaDobrota + "\n");
        }

        // selekcija: naci dobrotu cijele populacije, racunati dobrotu svake jedinke i
        // baciti u smece one kod kojih je omjer
        // dobrota pojedinca / dobrota populacije najmanji(10 komada)
        // onda nasumicno krizati ovih 10 koje ostanu i kopirati njih i njihovu djecu u
        // iducu iteraciju populacije

    }
    public static ArrayList<Integer> mutacija(ArrayList<Integer> populacija, Double vjerojatnostMutacije) {
        Random rand=new Random();
        int indexNajboljeJedinke = nadiNajboljuJedinku(populacija);
        for (int k=0;k<populacija.size();k++) {
            //provjeravamo je li odabrana jedinka zapravo elitna, ako da preskačemo mutaciju za tu jedinku
            if (k==indexNajboljeJedinke) continue;
            //popunjavmo string s vodecim nulama da bi duljina kromosoma bila uniformna za sve jedinke
            String nule = "00000000000";
            String jedinkaBit = Integer.toBinaryString(populacija.get(k));
            String brojNula = nule.substring(0, 10 - jedinkaBit.length());
            jedinkaBit = brojNula.concat(jedinkaBit);
            //prolazimo svaki bit jedan po jedan i gledamo je li odabran za mutaciju sa šansom vrijednosti mutacije

            for (int i = 0; i < jedinkaBit.length(); i++) {
                if (rand.nextDouble() <= vjerojatnostMutacije) {
                    //ako je bit odabran za mutaciju invertiraj ga
                    char[] mutatorChar = jedinkaBit.toCharArray();
                    if (mutatorChar[i] == '1') {
                        mutatorChar[i]='0';
                    } else {
                        mutatorChar[i]='1';
                    }
                    jedinkaBit = String.valueOf(mutatorChar);
                }
            }
            //dodajmo mutiranu jedinku umjesto stare
            Integer mutiranaJednika=Integer.parseInt(jedinkaBit, 2);
            populacija.set(k, mutiranaJednika);
        }

        return populacija;
    }
    public static ArrayList<Integer> krizanje(ArrayList<Integer> populacija, Double vjerojatnostKrizanja, Integer velicinaPopulacije, Double vjerojatnostMutacije) {
        //postavljamo prvu jedinku kao privremenu elitnu jedinku i zatim prolazimo populacijom i trazimo stvarnu elitnu jedinku
        int indexNajboljeJedinke = nadiNajboljuJedinku(populacija);

        ArrayList<Integer> novaPopulacija = new ArrayList<>();
        Set<Integer> jedinkeZaBrisanjeIndex = new HashSet<>();
        Random rand = new Random();
        //provodimo krizanje dok populacija ne dostigne pocetnu razinu
        do {
            //uzimamo dva nasumično odabrana roditelja
            int indexPrvogRod;
            int indexDrugogRod;
            do {
                indexPrvogRod = rand.nextInt(populacija.size());
                indexDrugogRod = rand.nextInt(populacija.size());
            } while (rand.nextDouble() >= vjerojatnostKrizanja || indexPrvogRod == indexDrugogRod);

            String bitPrvoDijete = "";
            String bitDrugoDijete = "";
            //pamtimo koje jedinke su sudjelovale u krizanju da ih mozemo obrisati - provjeravamo elitizam
            if (indexPrvogRod != indexNajboljeJedinke) jedinkeZaBrisanjeIndex.add(indexPrvogRod);
            if (indexDrugogRod != indexNajboljeJedinke) jedinkeZaBrisanjeIndex.add(indexDrugogRod);

            // popunjavmo string s vodecim nulama da bi duljina kromosoma bila uniformna za sve jedinke
            String nule = "00000000000";
            String bitPrvogRod = Integer.toBinaryString(populacija.get(indexPrvogRod));
            String brojNula = nule.substring(0, 10 - bitPrvogRod.length());
            bitPrvogRod = brojNula.concat(bitPrvogRod);
            String bitDrugogRod = Integer.toBinaryString(populacija.get(indexDrugogRod));
            brojNula = nule.substring(0, 10 - bitDrugogRod.length());
            bitDrugogRod = brojNula.concat(bitDrugogRod);

            //prolazimo kroz binarni zapis i gledamo imaju li jednake bitove, ako da prepisujemo ih u oba dijeteta
            //ako ne prepisujemo bit jednog od slucajno odabranog roditelja
            for (int i = 0; i < bitPrvogRod.length(); i++) {
                if (bitPrvogRod.charAt(i) == bitDrugogRod.charAt(i)) {
                    bitPrvoDijete += bitPrvogRod.charAt(i);
                    bitDrugoDijete += bitPrvogRod.charAt(i);
                    continue;
                }
                int odabir = rand.nextInt(2);
                if (odabir == 0) {
                    bitPrvoDijete += bitPrvogRod.charAt(i);
                } else {
                    bitPrvoDijete += bitDrugogRod.charAt(i);
                }
                odabir = rand.nextInt(2);
                if (odabir == 0) {
                    bitDrugoDijete += bitDrugogRod.charAt(i);
                } else {
                    bitDrugoDijete += bitPrvogRod.charAt(i);
                }

            }

            //dodajemo dijete populaciji
            Integer prvoDijete = Integer.parseInt(bitPrvoDijete, 2);
            Integer drugoDijete = Integer.parseInt(bitDrugoDijete, 2);

            //uklanjamo blizance
            while (prvoDijete == drugoDijete) {
                nule = "00000000000";
                String jedinkaBit = Integer.toBinaryString(prvoDijete);
                brojNula = nule.substring(0, 10 - jedinkaBit.length());
                jedinkaBit = brojNula.concat(jedinkaBit);
                //prolazimo svaki bit jedan po jedan i gledamo je li odabran za mutaciju sa šansom vrijednosti mutacije

                for (int i = 0; i < jedinkaBit.length(); i++) {
                    if (rand.nextDouble() <= vjerojatnostMutacije) {
                        //ako je bit odabran za mutaciju invertiraj ga
                        char[] mutatorChar = jedinkaBit.toCharArray();
                        if (mutatorChar[i] == '1') {
                            mutatorChar[i] = '0';
                        } else {
                            mutatorChar[i] = '1';
                        }
                        jedinkaBit = String.valueOf(mutatorChar);
                    }
                }
                //dodajmo mutiranu jedinku umjesto stare
                prvoDijete = Integer.parseInt(jedinkaBit, 2);

                jedinkaBit = Integer.toBinaryString(drugoDijete);
                brojNula = nule.substring(0, 10 - jedinkaBit.length());
                jedinkaBit = brojNula.concat(jedinkaBit);
                //prolazimo svaki bit jedan po jedan i gledamo je li odabran za mutaciju sa šansom vrijednosti mutacije

                for (int i = 0; i < jedinkaBit.length(); i++) {
                    if (rand.nextDouble() <= vjerojatnostMutacije) {
                        //ako je bit odabran za mutaciju invertiraj ga
                        char[] mutatorChar = jedinkaBit.toCharArray();
                        if (mutatorChar[i] == '1') {
                            mutatorChar[i] = '0';
                        } else {
                            mutatorChar[i] = '1';
                        }
                        jedinkaBit = String.valueOf(mutatorChar);
                    }
                }
                //dodajmo mutiranu jedinku umjesto stare
                drugoDijete = Integer.parseInt(jedinkaBit, 2);

            }

            if (novaPopulacija.size() + populacija.size() - jedinkeZaBrisanjeIndex.size() == velicinaPopulacije - 1) {
                if(rand.nextBoolean())
                    novaPopulacija.add(prvoDijete);
                else
                    novaPopulacija.add(drugoDijete);
            }
            else
            {
                novaPopulacija.add(prvoDijete);
                novaPopulacija.add(drugoDijete);
            }

            //postupak križanja ponavljamo dok se populacija koju čine djeca i roditelji koji nisu krizani ne vrati na početnu vrijednost
        }while (novaPopulacija.size() + populacija.size() - jedinkeZaBrisanjeIndex.size()<velicinaPopulacije);
        //mičemo roditelje koji su se koristili u krizanju
        List<Integer> sortiraniIndexiRoditelja = jedinkeZaBrisanjeIndex.stream().collect(Collectors.toList());
        Collections.sort(sortiraniIndexiRoditelja, Collections.reverseOrder());
        for (int i : sortiraniIndexiRoditelja)
            populacija.remove(i);
        //dodamo roditelje koji se nisu krizali u novu generaciju
        novaPopulacija.addAll(populacija);
        return novaPopulacija;
    }

    public static int nadiNajboljuJedinku(ArrayList<Integer> populacija) {
        double maxDobrota =dobrota(populacija.get(0));
        int indexElitneJedinke=0;
        for (int i=0;i<populacija.size();i++) {
            if (dobrota(populacija.get(i)) > maxDobrota) {
                maxDobrota=dobrota(populacija.get(i));
                indexElitneJedinke=i;
            }
        }
        return indexElitneJedinke;
    }

    public static ArrayList<Integer> selekcija(ArrayList<Integer> populacija) {
        ArrayList<Integer> novaPopulacija = new ArrayList<>();
        Map<Integer, Double> privremenaLista = new HashMap<>();
        Double dobrotaPopulacije = 0.0;
        // racunamo dobrotu svake jedinke, spremamo u mapu za kasnije sortiranje i
        // odbabir top 10 jedinki
        for (int i = 0; i < populacija.size(); i++) {
            double dobrotaJedinke = dobrota(populacija.get(i));
            privremenaLista.put(i, dobrotaJedinke);
            dobrotaPopulacije += dobrotaJedinke;
        }
        dobrotaPopulacije = dobrotaPopulacije / populacija.size();
        // sortiramo hashmap kako bi poredali jedinke po dobroti i izbacujemo najgoru polovicu
        Map<Integer, Double> sortiranaMapa = privremenaLista.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        int limit = sortiranaMapa.size()/2;
        List<Integer> indexiNajboljihKandidata = sortiranaMapa.keySet().stream().limit(limit).collect(Collectors.toList());

        for (Integer index : indexiNajboljihKandidata) {
            novaPopulacija.add(populacija.get(index));
        }
        return novaPopulacija;
    }

    public static ArrayList<Integer> stvoriPopulaciju(Integer velicinaPopulacije) {
        ArrayList<Integer> populacija = new ArrayList<>();
        Random jedinka = new Random();
        for (int i = 0; i < velicinaPopulacije; i++) {
            populacija.add(jedinka.nextInt(1024));// stvara random broj od 0-1023
        }
        return populacija;
    }

    public static double dobrota(int jedinka) {
        // Funkcija racuna dobrotu jedinke (int jedinka) prema funkciji prikaznoj u
        // tekstu zadatka
        // Dozvoljene ulazne vrijednosti su u otvorenom intervalu [0, 1023]
        // Funkcija vraca -1 ako je zadana nedozvoljena vrijednost

        if (jedinka < 0 || jedinka >= 1024) {
            return -1;
        }

        if (jedinka >= 0 && jedinka < 30) {
            return 60.0;
        } else if (jedinka >= 30 && jedinka < 90) {
            return (double) jedinka + 30.0;
        } else if (jedinka >= 90 && jedinka < 120) {
            return 120.0;
        } else if (jedinka >= 120 && jedinka < 210) {
            return -0.83333 * (double) jedinka + 220;
        } else if (jedinka >= 210 && jedinka < 270) {
            return 1.75 * (double) jedinka - 322.5;
        } else if (jedinka >= 270 && jedinka < 300) {
            return 150.0;
        } else if (jedinka >= 300 && jedinka < 360) {
            return 2.0 * (double) jedinka - 450;
        } else if (jedinka >= 360 && jedinka < 510) {
            return -1.8 * (double) jedinka + 918;
        } else if (jedinka >= 510 && jedinka < 630) {
            return 1.5 * (double) jedinka - 765;
        } else if (jedinka >= 630 && jedinka < 720) {
            return -1.33333 * (double) jedinka + 1020;
        } else if (jedinka >= 720 && jedinka < 750) {
            return 60.0;
        } else if (jedinka >= 750 && jedinka < 870) {
            return 1.5 * (double) jedinka - 1065;
        } else if (jedinka >= 870 && jedinka < 960) {
            return -2.66667 * (double) jedinka + 2560;
        } else {
            return 0;
        }
    }

}
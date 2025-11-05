package com.war.game.war_backend.config;

import com.war.game.war_backend.model.Territory;
import com.war.game.war_backend.model.TerritoryBorder;
import com.war.game.war_backend.repository.TerritoryBorderRepository;
import com.war.game.war_backend.repository.TerritoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class TerritoryInitializerConfig {
    @Bean
    @Order(1) // Executa primeiro
    public CommandLineRunner territoryInitializer(
            TerritoryRepository territoryRepository, TerritoryBorderRepository borderRepository) {
        return args -> {
            System.out.println("=== TerritoryInitializer: Iniciando verificação ===");

            // Verificar se os dados já foram inicializados
            long territoryCount = territoryRepository.count();
            long borderCount = borderRepository.count();

            System.out.println(
                    "=== TerritoryInitializer: Territórios existentes: " + territoryCount + " ===");
            System.out.println(
                    "=== TerritoryInitializer: Bordas existentes: " + borderCount + " ===");

            if (territoryCount > 0) {
                System.out.println(
                        "=== TerritoryInitializer: Territórios já existem, pulando inicialização de territórios ===");

                // Verificar se as bordas foram criadas
                if (borderCount == 0) {
                    System.out.println("=== TerritoryInitializer: CRIANDO APENAS AS BORDAS ===");
                    // Buscar territórios existentes para criar as bordas
                    createBorders(territoryRepository, borderRepository);
                } else {
                    System.out.println(
                            "=== TerritoryInitializer: Bordas já existem, pulando tudo ===");
                }
                return;
            }

            System.out.println("=== TerritoryInitializer: Criando territórios ===");

            // América do Norte
            Territory alaska = new Territory();
            alaska.setName("ALASKA");
            alaska.setContinent("América do Norte");
            territoryRepository.save(alaska);

            Territory mackenzie = new Territory();
            mackenzie.setName("MACKENZIE");
            mackenzie.setContinent("América do Norte");
            territoryRepository.save(mackenzie);

            Territory vancouver = new Territory();
            vancouver.setName("VANCOUVER");
            vancouver.setContinent("América do Norte");
            territoryRepository.save(vancouver);

            Territory ottawa = new Territory();
            ottawa.setName("OTTAWA");
            ottawa.setContinent("América do Norte");
            territoryRepository.save(ottawa);

            Territory labrador = new Territory();
            labrador.setName("LABRADOR");
            labrador.setContinent("América do Norte");
            territoryRepository.save(labrador);

            Territory california = new Territory();
            california.setName("CALIFÓRNIA");
            california.setContinent("América do Norte");
            territoryRepository.save(california);

            Territory newYork = new Territory();
            newYork.setName("NOVA YORK");
            newYork.setContinent("América do Norte");
            territoryRepository.save(newYork);

            Territory mexico = new Territory();
            mexico.setName("MÉXICO");
            mexico.setContinent("América do Norte");
            territoryRepository.save(mexico);

            Territory greenland = new Territory();
            greenland.setName("GROENLÂNDIA");
            greenland.setContinent("América do Norte");
            territoryRepository.save(greenland);

            // América do Sul
            Territory venezuela = new Territory();
            venezuela.setName("VENEZUELA");
            venezuela.setContinent("América do Sul");
            territoryRepository.save(venezuela);

            Territory brasil = new Territory();
            brasil.setName("BRASIL");
            brasil.setContinent("América do Sul");
            territoryRepository.save(brasil);

            Territory bolivia = new Territory();
            bolivia.setName("BOLÍVIA");
            bolivia.setContinent("América do Sul");
            territoryRepository.save(bolivia);

            Territory argentina = new Territory();
            argentina.setName("ARGENTINA");
            argentina.setContinent("América do Sul");
            territoryRepository.save(argentina);

            // Europa
            Territory iceland = new Territory();
            iceland.setName("ISLÂNDIA");
            iceland.setContinent("Europa");
            territoryRepository.save(iceland);

            Territory england = new Territory();
            england.setName("INGLATERRA");
            england.setContinent("Europa");
            territoryRepository.save(england);

            Territory sweden = new Territory();
            sweden.setName("SUÉCIA");
            sweden.setContinent("Europa");
            territoryRepository.save(sweden);

            Territory poland = new Territory();
            poland.setName("POLÔNIA");
            poland.setContinent("Europa");
            territoryRepository.save(poland);

            Territory italy = new Territory();
            italy.setName("ITÁLIA");
            italy.setContinent("Europa");
            territoryRepository.save(italy);

            Territory spain = new Territory();
            spain.setName("ESPANHA");
            spain.setContinent("Europa");
            territoryRepository.save(spain);

            Territory moscow = new Territory();
            moscow.setName("MOSCOU");
            moscow.setContinent("Europa");
            territoryRepository.save(moscow);

            // África
            Territory egypt = new Territory();
            egypt.setName("EGITO");
            egypt.setContinent("África");
            territoryRepository.save(egypt);

            Territory nigeria = new Territory();
            nigeria.setName("NIGÉRIA");
            nigeria.setContinent("África");
            territoryRepository.save(nigeria);

            Territory sudan = new Territory();
            sudan.setName("SUDÃO");
            sudan.setContinent("África");
            territoryRepository.save(sudan);

            Territory congo = new Territory();
            congo.setName("CONGO");
            congo.setContinent("África");
            territoryRepository.save(congo);

            Territory southAfrica = new Territory();
            southAfrica.setName("ÁFRICA DO SUL");
            southAfrica.setContinent("África");
            territoryRepository.save(southAfrica);

            Territory madagascar = new Territory();
            madagascar.setName("MADAGASCAR");
            madagascar.setContinent("África");
            territoryRepository.save(madagascar);

            // Ásia
            Territory omsk = new Territory();
            omsk.setName("OMSK");
            omsk.setContinent("Ásia");
            territoryRepository.save(omsk);

            Territory dudinka = new Territory();
            dudinka.setName("DUDINKA");
            dudinka.setContinent("Ásia");
            territoryRepository.save(dudinka);

            Territory siberia = new Territory();
            siberia.setName("SIBÉRIA");
            siberia.setContinent("Ásia");
            territoryRepository.save(siberia);

            Territory vladivostok = new Territory();
            vladivostok.setName("VLADIVOSTOK");
            vladivostok.setContinent("Ásia");
            territoryRepository.save(vladivostok);

            Territory tchita = new Territory();
            tchita.setName("TCHITA");
            tchita.setContinent("Ásia");
            territoryRepository.save(tchita);

            Territory mongolia = new Territory();
            mongolia.setName("MONGÓLIA");
            mongolia.setContinent("Ásia");
            territoryRepository.save(mongolia);

            Territory japan = new Territory();
            japan.setName("JAPÃO");
            japan.setContinent("Ásia");
            territoryRepository.save(japan);

            Territory aral = new Territory();
            aral.setName("ARAL");
            aral.setContinent("Ásia");
            territoryRepository.save(aral);

            Territory china = new Territory();
            china.setName("CHINA");
            china.setContinent("Ásia");
            territoryRepository.save(china);

            Territory india = new Territory();
            india.setName("ÍNDIA");
            india.setContinent("Ásia");
            territoryRepository.save(india);

            Territory vietnam = new Territory();
            vietnam.setName("VIETNÃ");
            vietnam.setContinent("Ásia");
            territoryRepository.save(vietnam);

            Territory middleEast = new Territory();
            middleEast.setName("ORIENTE MÉDIO");
            middleEast.setContinent("Ásia");
            territoryRepository.save(middleEast);

            // Oceania
            Territory australia = new Territory();
            australia.setName("AUSTRÁLIA");
            australia.setContinent("Oceania");
            territoryRepository.save(australia);

            Territory sumatra = new Territory();
            sumatra.setName("SUMATRA");
            sumatra.setContinent("Oceania");
            territoryRepository.save(sumatra);

            Territory borneo = new Territory();
            borneo.setName("BORNEO");
            borneo.setContinent("Oceania");
            territoryRepository.save(borneo);

            Territory newGuinea = new Territory();
            newGuinea.setName("NOVA GUINÉ");
            newGuinea.setContinent("Oceania");
            territoryRepository.save(newGuinea);

            System.out.println("=== TerritoryInitializer: 40 territórios criados com sucesso ===");
            System.out.println("=== TerritoryInitializer: Criando bordas ===");

            // Criar bordas
            createBorders(territoryRepository, borderRepository);

            System.out.println("=== TerritoryInitializer: Finalizado com sucesso ===");
        };
    }

    private void createBorders(
            TerritoryRepository territoryRepository, TerritoryBorderRepository borderRepository) {
        // Buscar todos os territórios pelo nome
        Territory alaska = territoryRepository.findByName("ALASKA").orElse(null);
        Territory mackenzie = territoryRepository.findByName("MACKENZIE").orElse(null);
        Territory vancouver = territoryRepository.findByName("VANCOUVER").orElse(null);
        Territory ottawa = territoryRepository.findByName("OTTAWA").orElse(null);
        Territory labrador = territoryRepository.findByName("LABRADOR").orElse(null);
        Territory califórnia = territoryRepository.findByName("CALIFÓRNIA").orElse(null);
        Territory novaYork = territoryRepository.findByName("NOVA YORK").orElse(null);
        Territory méxico = territoryRepository.findByName("MÉXICO").orElse(null);
        Territory groenlandia = territoryRepository.findByName("GROENLÂNDIA").orElse(null);

        Territory venezuela = territoryRepository.findByName("VENEZUELA").orElse(null);
        Territory brasil = territoryRepository.findByName("BRASIL").orElse(null);
        Territory bolivia = territoryRepository.findByName("BOLÍVIA").orElse(null);
        Territory argentina = territoryRepository.findByName("ARGENTINA").orElse(null);

        Territory islândia = territoryRepository.findByName("ISLÂNDIA").orElse(null);
        Territory inglaterra = territoryRepository.findByName("INGLATERRA").orElse(null);
        Territory suécia = territoryRepository.findByName("SUÉCIA").orElse(null);
        Territory polônia = territoryRepository.findByName("POLÔNIA").orElse(null);
        Territory itália = territoryRepository.findByName("ITÁLIA").orElse(null);
        Territory espanha = territoryRepository.findByName("ESPANHA").orElse(null);
        Territory moscou = territoryRepository.findByName("MOSCOU").orElse(null);

        Territory egito = territoryRepository.findByName("EGITO").orElse(null);
        Territory nigéria = territoryRepository.findByName("NIGÉRIA").orElse(null);
        Territory sudão = territoryRepository.findByName("SUDÃO").orElse(null);
        Territory congo = territoryRepository.findByName("CONGO").orElse(null);
        Territory africaSul = territoryRepository.findByName("ÁFRICA DO SUL").orElse(null);
        Territory madagascar = territoryRepository.findByName("MADAGASCAR").orElse(null);

        Territory omsk = territoryRepository.findByName("OMSK").orElse(null);
        Territory dudinka = territoryRepository.findByName("DUDINKA").orElse(null);
        Territory siberia = territoryRepository.findByName("SIBÉRIA").orElse(null);
        Territory vladivostok = territoryRepository.findByName("VLADIVOSTOK").orElse(null);
        Territory tchita = territoryRepository.findByName("TCHITA").orElse(null);
        Territory mongólia = territoryRepository.findByName("MONGÓLIA").orElse(null);
        Territory japao = territoryRepository.findByName("JAPÃO").orElse(null);
        Territory aral = territoryRepository.findByName("ARAL").orElse(null);
        Territory china = territoryRepository.findByName("CHINA").orElse(null);
        Territory india = territoryRepository.findByName("ÍNDIA").orElse(null);
        Territory vietna = territoryRepository.findByName("VIETNÃ").orElse(null);
        Territory orienteMedio = territoryRepository.findByName("ORIENTE MÉDIO").orElse(null);

        Territory australia = territoryRepository.findByName("AUSTRÁLIA").orElse(null);
        Territory sumatra = territoryRepository.findByName("SUMATRA").orElse(null);
        Territory borneo = territoryRepository.findByName("BORNEO").orElse(null);
        Territory novaGuine = territoryRepository.findByName("NOVA GUINÉ").orElse(null);

        int bordersCreated = 0;

        // Fronteiras conforme o mapa do War

        // América do Norte - Fronteiras internas
        TerritoryBorder border1 = new TerritoryBorder();
        border1.setTerritoryA(alaska);
        border1.setTerritoryB(mackenzie);
        borderRepository.save(border1);
        bordersCreated++;

        TerritoryBorder border2 = new TerritoryBorder();
        border2.setTerritoryA(alaska);
        border2.setTerritoryB(vancouver);
        borderRepository.save(border2);
        bordersCreated++;

        TerritoryBorder border3 = new TerritoryBorder();
        border3.setTerritoryA(mackenzie);
        border3.setTerritoryB(vancouver);
        borderRepository.save(border3);
        bordersCreated++;

        TerritoryBorder border4 = new TerritoryBorder();
        border4.setTerritoryA(mackenzie);
        border4.setTerritoryB(ottawa);
        borderRepository.save(border4);
        bordersCreated++;

        TerritoryBorder border5 = new TerritoryBorder();
        border5.setTerritoryA(mackenzie);
        border5.setTerritoryB(groenlandia);
        borderRepository.save(border5);
        bordersCreated++;

        TerritoryBorder border6 = new TerritoryBorder();
        border6.setTerritoryA(vancouver);
        border6.setTerritoryB(ottawa);
        borderRepository.save(border6);
        bordersCreated++;

        TerritoryBorder border7 = new TerritoryBorder();
        border7.setTerritoryA(vancouver);
        border7.setTerritoryB(califórnia);
        borderRepository.save(border7);
        bordersCreated++;

        TerritoryBorder border8 = new TerritoryBorder();
        border8.setTerritoryA(ottawa);
        border8.setTerritoryB(labrador);
        borderRepository.save(border8);
        bordersCreated++;

        TerritoryBorder border9 = new TerritoryBorder();
        border9.setTerritoryA(ottawa);
        border9.setTerritoryB(novaYork);
        borderRepository.save(border9);
        bordersCreated++;

        TerritoryBorder border10 = new TerritoryBorder();
        border10.setTerritoryA(ottawa);
        border10.setTerritoryB(califórnia);
        borderRepository.save(border10);
        bordersCreated++;

        TerritoryBorder border11 = new TerritoryBorder();
        border11.setTerritoryA(ottawa);
        border11.setTerritoryB(groenlandia);
        borderRepository.save(border11);
        bordersCreated++;

        TerritoryBorder border12 = new TerritoryBorder();
        border12.setTerritoryA(labrador);
        border12.setTerritoryB(groenlandia);
        borderRepository.save(border12);
        bordersCreated++;

        TerritoryBorder border13 = new TerritoryBorder();
        border13.setTerritoryA(labrador);
        border13.setTerritoryB(novaYork);
        borderRepository.save(border13);
        bordersCreated++;

        TerritoryBorder border14 = new TerritoryBorder();
        border14.setTerritoryA(califórnia);
        border14.setTerritoryB(novaYork);
        borderRepository.save(border14);
        bordersCreated++;

        TerritoryBorder border15 = new TerritoryBorder();
        border15.setTerritoryA(califórnia);
        border15.setTerritoryB(méxico);
        borderRepository.save(border15);
        bordersCreated++;

        TerritoryBorder border16 = new TerritoryBorder();
        border16.setTerritoryA(novaYork);
        border16.setTerritoryB(méxico);
        borderRepository.save(border16);
        bordersCreated++;

        // América do Sul - Fronteiras internas
        TerritoryBorder border17 = new TerritoryBorder();
        border17.setTerritoryA(venezuela);
        border17.setTerritoryB(brasil);
        borderRepository.save(border17);
        bordersCreated++;

        TerritoryBorder border18 = new TerritoryBorder();
        border18.setTerritoryA(venezuela);
        border18.setTerritoryB(bolivia);
        borderRepository.save(border18);
        bordersCreated++;

        TerritoryBorder border19 = new TerritoryBorder();
        border19.setTerritoryA(brasil);
        border19.setTerritoryB(bolivia);
        borderRepository.save(border19);
        bordersCreated++;

        TerritoryBorder border20 = new TerritoryBorder();
        border20.setTerritoryA(brasil);
        border20.setTerritoryB(argentina);
        borderRepository.save(border20);
        bordersCreated++;

        TerritoryBorder border21 = new TerritoryBorder();
        border21.setTerritoryA(bolivia);
        border21.setTerritoryB(argentina);
        borderRepository.save(border21);
        bordersCreated++;

        // Europa - Fronteiras internas
        TerritoryBorder border22 = new TerritoryBorder();
        border22.setTerritoryA(islândia);
        border22.setTerritoryB(inglaterra);
        borderRepository.save(border22);
        bordersCreated++;

        TerritoryBorder border23 = new TerritoryBorder();
        border23.setTerritoryA(islândia);
        border23.setTerritoryB(suécia);
        borderRepository.save(border23);
        bordersCreated++;

        TerritoryBorder border24 = new TerritoryBorder();
        border24.setTerritoryA(inglaterra);
        border24.setTerritoryB(suécia);
        borderRepository.save(border24);
        bordersCreated++;

        TerritoryBorder border25 = new TerritoryBorder();
        border25.setTerritoryA(inglaterra);
        border25.setTerritoryB(polônia);
        borderRepository.save(border25);
        bordersCreated++;

        TerritoryBorder border26 = new TerritoryBorder();
        border26.setTerritoryA(inglaterra);
        border26.setTerritoryB(espanha);
        borderRepository.save(border26);
        bordersCreated++;

        TerritoryBorder border27 = new TerritoryBorder();
        border27.setTerritoryA(suécia);
        border27.setTerritoryB(polônia);
        borderRepository.save(border27);
        bordersCreated++;

        TerritoryBorder border28 = new TerritoryBorder();
        border28.setTerritoryA(suécia);
        border28.setTerritoryB(moscou);
        borderRepository.save(border28);
        bordersCreated++;

        TerritoryBorder border29 = new TerritoryBorder();
        border29.setTerritoryA(polônia);
        border29.setTerritoryB(moscou);
        borderRepository.save(border29);
        bordersCreated++;

        TerritoryBorder border30 = new TerritoryBorder();
        border30.setTerritoryA(polônia);
        border30.setTerritoryB(itália);
        borderRepository.save(border30);
        bordersCreated++;

        TerritoryBorder border31 = new TerritoryBorder();
        border31.setTerritoryA(polônia);
        border31.setTerritoryB(espanha);
        borderRepository.save(border31);
        bordersCreated++;

        TerritoryBorder border32 = new TerritoryBorder();
        border32.setTerritoryA(itália);
        border32.setTerritoryB(espanha);
        borderRepository.save(border32);
        bordersCreated++;

        TerritoryBorder border33 = new TerritoryBorder();
        border33.setTerritoryA(itália);
        border33.setTerritoryB(moscou);
        borderRepository.save(border33);
        bordersCreated++;

        // África - Fronteiras internas
        TerritoryBorder border34 = new TerritoryBorder();
        border34.setTerritoryA(egito);
        border34.setTerritoryB(sudão);
        borderRepository.save(border34);
        bordersCreated++;

        TerritoryBorder border35 = new TerritoryBorder();
        border35.setTerritoryA(egito);
        border35.setTerritoryB(nigéria);
        borderRepository.save(border35);
        bordersCreated++;

        TerritoryBorder border36 = new TerritoryBorder();
        border36.setTerritoryA(nigéria);
        border36.setTerritoryB(sudão);
        borderRepository.save(border36);
        bordersCreated++;

        TerritoryBorder border37 = new TerritoryBorder();
        border37.setTerritoryA(nigéria);
        border37.setTerritoryB(congo);
        borderRepository.save(border37);
        bordersCreated++;

        TerritoryBorder border38 = new TerritoryBorder();
        border38.setTerritoryA(sudão);
        border38.setTerritoryB(congo);
        borderRepository.save(border38);
        bordersCreated++;

        TerritoryBorder border39 = new TerritoryBorder();
        border39.setTerritoryA(sudão);
        border39.setTerritoryB(africaSul);
        borderRepository.save(border39);
        bordersCreated++;

        TerritoryBorder border40 = new TerritoryBorder();
        border40.setTerritoryA(sudão);
        border40.setTerritoryB(madagascar);
        borderRepository.save(border40);
        bordersCreated++;

        TerritoryBorder border41 = new TerritoryBorder();
        border41.setTerritoryA(congo);
        border41.setTerritoryB(africaSul);
        borderRepository.save(border41);
        bordersCreated++;

        TerritoryBorder border42 = new TerritoryBorder();
        border42.setTerritoryA(africaSul);
        border42.setTerritoryB(madagascar);
        borderRepository.save(border42);
        bordersCreated++;

        // Ásia - Fronteiras internas
        TerritoryBorder border43 = new TerritoryBorder();
        border43.setTerritoryA(omsk);
        border43.setTerritoryB(dudinka);
        borderRepository.save(border43);
        bordersCreated++;

        TerritoryBorder border44 = new TerritoryBorder();
        border44.setTerritoryA(omsk);
        border44.setTerritoryB(china);
        borderRepository.save(border44);
        bordersCreated++;

        TerritoryBorder border45 = new TerritoryBorder();
        border45.setTerritoryA(omsk);
        border45.setTerritoryB(aral);
        borderRepository.save(border45);
        bordersCreated++;

        TerritoryBorder border46 = new TerritoryBorder();
        border46.setTerritoryA(dudinka);
        border46.setTerritoryB(siberia);
        borderRepository.save(border46);
        bordersCreated++;

        TerritoryBorder border47 = new TerritoryBorder();
        border47.setTerritoryA(dudinka);
        border47.setTerritoryB(tchita);
        borderRepository.save(border47);
        bordersCreated++;

        TerritoryBorder border48 = new TerritoryBorder();
        border48.setTerritoryA(dudinka);
        border48.setTerritoryB(mongólia);
        borderRepository.save(border48);
        bordersCreated++;

        TerritoryBorder border49 = new TerritoryBorder();
        border49.setTerritoryA(dudinka);
        border49.setTerritoryB(china);
        borderRepository.save(border49);
        bordersCreated++;

        TerritoryBorder border50 = new TerritoryBorder();
        border50.setTerritoryA(siberia);
        border50.setTerritoryB(tchita);
        borderRepository.save(border50);
        bordersCreated++;

        TerritoryBorder border51 = new TerritoryBorder();
        border51.setTerritoryA(siberia);
        border51.setTerritoryB(vladivostok);
        borderRepository.save(border51);
        bordersCreated++;

        TerritoryBorder border52 = new TerritoryBorder();
        border52.setTerritoryA(tchita);
        border52.setTerritoryB(vladivostok);
        borderRepository.save(border52);
        bordersCreated++;

        TerritoryBorder border53 = new TerritoryBorder();
        border53.setTerritoryA(tchita);
        border53.setTerritoryB(mongólia);
        borderRepository.save(border53);
        bordersCreated++;

        TerritoryBorder border54 = new TerritoryBorder();
        border54.setTerritoryA(vladivostok);
        border54.setTerritoryB(mongólia);
        borderRepository.save(border54);
        bordersCreated++;

        TerritoryBorder border55 = new TerritoryBorder();
        border55.setTerritoryA(vladivostok);
        border55.setTerritoryB(japao);
        borderRepository.save(border55);
        bordersCreated++;

        TerritoryBorder border56 = new TerritoryBorder();
        border56.setTerritoryA(mongólia);
        border56.setTerritoryB(china);
        borderRepository.save(border56);
        bordersCreated++;

        TerritoryBorder border57 = new TerritoryBorder();
        border57.setTerritoryA(mongólia);
        border57.setTerritoryB(japao);
        borderRepository.save(border57);
        bordersCreated++;

        TerritoryBorder border58 = new TerritoryBorder();
        border58.setTerritoryA(aral);
        border58.setTerritoryB(china);
        borderRepository.save(border58);
        bordersCreated++;

        TerritoryBorder border59 = new TerritoryBorder();
        border59.setTerritoryA(aral);
        border59.setTerritoryB(india);
        borderRepository.save(border59);
        bordersCreated++;

        TerritoryBorder border60 = new TerritoryBorder();
        border60.setTerritoryA(aral);
        border60.setTerritoryB(orienteMedio);
        borderRepository.save(border60);
        bordersCreated++;

        TerritoryBorder border61 = new TerritoryBorder();
        border61.setTerritoryA(china);
        border61.setTerritoryB(india);
        borderRepository.save(border61);
        bordersCreated++;

        TerritoryBorder border62 = new TerritoryBorder();
        border62.setTerritoryA(china);
        border62.setTerritoryB(vietna);
        borderRepository.save(border62);
        bordersCreated++;

        TerritoryBorder border63 = new TerritoryBorder();
        border63.setTerritoryA(india);
        border63.setTerritoryB(vietna);
        borderRepository.save(border63);
        bordersCreated++;

        TerritoryBorder border64 = new TerritoryBorder();
        border64.setTerritoryA(india);
        border64.setTerritoryB(orienteMedio);
        borderRepository.save(border64);
        bordersCreated++;

        // Oceania - Fronteiras internas
        TerritoryBorder border65 = new TerritoryBorder();
        border65.setTerritoryA(sumatra);
        border65.setTerritoryB(australia);
        borderRepository.save(border65);
        bordersCreated++;

        TerritoryBorder border66 = new TerritoryBorder();
        border66.setTerritoryA(borneo);
        border66.setTerritoryB(novaGuine);
        borderRepository.save(border66);
        bordersCreated++;

        TerritoryBorder border67 = new TerritoryBorder();
        border67.setTerritoryA(borneo);
        border67.setTerritoryB(australia);
        borderRepository.save(border67);
        bordersCreated++;

        TerritoryBorder border68 = new TerritoryBorder();
        border68.setTerritoryA(novaGuine);
        border68.setTerritoryB(australia);
        borderRepository.save(border68);
        bordersCreated++;

        // Fronteiras intercontinentais
        // Oceania - Ásia
        TerritoryBorder border69 = new TerritoryBorder();
        border69.setTerritoryA(borneo);
        border69.setTerritoryB(vietna);
        borderRepository.save(border69);
        bordersCreated++;

        TerritoryBorder border70 = new TerritoryBorder();
        border70.setTerritoryA(sumatra);
        border70.setTerritoryB(india);
        borderRepository.save(border70);
        bordersCreated++;

        // Ásia - Africa
        TerritoryBorder border71 = new TerritoryBorder();
        border71.setTerritoryA(orienteMedio);
        border71.setTerritoryB(sudão);
        borderRepository.save(border71);
        bordersCreated++;

        TerritoryBorder border72 = new TerritoryBorder();
        border72.setTerritoryA(orienteMedio);
        border72.setTerritoryB(egito);
        borderRepository.save(border72);
        bordersCreated++;

        // Ásia - América do Norte
        TerritoryBorder border73 = new TerritoryBorder();
        border73.setTerritoryA(vladivostok);
        border73.setTerritoryB(alaska);
        borderRepository.save(border73);
        bordersCreated++;

        // Ásia - Europa
        TerritoryBorder border74 = new TerritoryBorder();
        border74.setTerritoryA(omsk);
        border74.setTerritoryB(moscou);
        borderRepository.save(border74);
        bordersCreated++;

        TerritoryBorder border75 = new TerritoryBorder();
        border75.setTerritoryA(aral);
        border75.setTerritoryB(moscou);
        borderRepository.save(border75);
        bordersCreated++;

        TerritoryBorder border76 = new TerritoryBorder();
        border76.setTerritoryA(orienteMedio);
        border76.setTerritoryB(moscou);
        borderRepository.save(border76);
        bordersCreated++;

        TerritoryBorder border77 = new TerritoryBorder();
        border77.setTerritoryA(orienteMedio);
        border77.setTerritoryB(itália);
        borderRepository.save(border77);
        bordersCreated++;

        // África - Europa
        TerritoryBorder border78 = new TerritoryBorder();
        border78.setTerritoryA(egito);
        border78.setTerritoryB(itália);
        borderRepository.save(border78);
        bordersCreated++;

        TerritoryBorder border79 = new TerritoryBorder();
        border79.setTerritoryA(nigéria);
        border79.setTerritoryB(espanha);
        borderRepository.save(border79);
        bordersCreated++;

        // África - América do Sul
        TerritoryBorder border80 = new TerritoryBorder();
        border80.setTerritoryA(nigéria);
        border80.setTerritoryB(brasil);
        borderRepository.save(border80);
        bordersCreated++;

        // Europa - América do Norte
        TerritoryBorder border81 = new TerritoryBorder();
        border81.setTerritoryA(islândia);
        border81.setTerritoryB(groenlandia);
        borderRepository.save(border81);
        bordersCreated++;

        // América do Sul - América do Norte
        TerritoryBorder border82 = new TerritoryBorder();
        border82.setTerritoryA(méxico);
        border82.setTerritoryB(venezuela);
        borderRepository.save(border82);
        bordersCreated++;

        System.out.println(
                "=== TerritoryInitializer: " + bordersCreated + " bordas criadas com sucesso ===");
    }
}

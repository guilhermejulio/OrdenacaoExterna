package app;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;


public class App {
    /*
     * // FORMATO DO ARQUIVO: PESSOAS // CABEÇALHO: INT COM Nº DE REGISTROS // RG:
     * INT // NOME: STRING UTF // NASCIMENTO: STRING UTF
     *
     */

    static final String path = "Pessoas.bin";
    static final int TAMBLOCO = 1000; // tamanho do bloco em memória principal
    static final int QTARQ = 3; // quantidade de fontes de dados (devem ser usadas para leitura e para escrita)
    static int QTDPESSOAS = 0;
    static BlockSet conjuntoBlockSet;

    static boolean divisaoRealizada = false;

    static int qtdBlocosGerados = 0;
    static int qtdPassadasNoArquivo =0;

    public static void gerarBlocosFixos() throws IOException {
        // Array de pessoas contendo as pessoas que representaram 1 bloco e que será
        // gravado no arquivo
        qtdBlocosGerados =0;
        qtdPassadasNoArquivo =0;
        Pessoa[] blocoPessoas = new Pessoa[TAMBLOCO];
        int posAtual = 0; // marca a posição atual da leitura do arquivo inteiro
        int arqAtual = 0; // marca qual arquivo temporário está sendo usado para gravação do bloco
                          // ordenado

        // realizar a leitura em memoria dos dados das pessoas
        RandomAccessFile arqDados = new RandomAccessFile(new File(path), "r");

        // arquivos temporarios onde serão gravados os blocos
        conjuntoBlockSet = new BlockSet(QTARQ);

        conjuntoBlockSet.inicializarArquivosBlocos();
        QTDPESSOAS = arqDados.readInt();

        for(int i=0; i< QTDPESSOAS;i++){
            //para cada registro dentro do arquivo orignal, é lido uma pessoa  
            Pessoa aux = Pessoa.readFromFile(arqDados);
            blocoPessoas[posAtual] = aux; //salvo a pessoa no bloco de pessoas
            posAtual++; // incremento a posição atual do bloco que estou salvando as pessoas

            //verificação se o bloco está cheio
            if(posAtual == TAMBLOCO){   

                //gravação do bloco no arquivo atual que estou trabalhando
                gravarBloco(blocoPessoas, conjuntoBlockSet.getArquivoEscrita()[arqAtual]);    
                qtdBlocosGerados++;
                posAtual=0;                                     //limpeza para o próximo bloco
                Arrays.fill(blocoPessoas, Pessoa.separador());  //limpamos os dados com o "separador"
                
                
                arqAtual++; 
                arqAtual = (arqAtual % QTARQ);  //passar para o próximo arquivo.
                
                                    
                //
                //  ...........X...........X...........X...........
                //  ...........X...........X...........X.....XXXXXX
                //  ...........X...........X...........XXXXXXXXXXXX
                //
            }
        }

        //se existe alguma pessoa no bloco de pessoas que ainda não foi gravada
        //no caso essa pessoa não é um separador
        //ou seja está no bloco e não foi gravado no arquivo
        if(!blocoPessoas[0].EUmSeparador()){
            gravarBloco(blocoPessoas, conjuntoBlockSet.getArquivoEscrita()[arqAtual]);
            qtdBlocosGerados++;
        }
        qtdPassadasNoArquivo++;
        
        //fechando todos arquivos de escrita para não ocorrer nenhum problema
        conjuntoBlockSet.fecharTodosArquivosEscrita();
        arqDados.close();
    }



    public static void gravarBloco(Pessoa[] bloco, RandomAccessFile arq) throws IOException{
        Arrays.sort(bloco);  //ordenar o bloco. polimorfismo universal paramétrico. (genérico)             
                                                
        for(Pessoa p : bloco){       // escrever alternadamente nos arquivos temporários
           //o bloco pode vir com metade do bloco com pessoas reais, e o restante separador
           //por esse motivo é necessario verificar se a pessoa é um separador antes de gravar
            if(!p.EUmSeparador()){         
                p.saveToFile(arq);
           }
        }

        Pessoa.separador().saveToFile(arq);
    }

    public static void intercalarBlocos() throws IOException {
        //Os blocos acabaram de ser escritos nos arquivos de escrita, 
        //Logo, os arquivos de escrita vão se tornar arquivos de leitura 
        conjuntoBlockSet.alternarFontes();
        conjuntoBlockSet.abrirArquivos();
        //crio meu extrator responsavel por extrair Pessoas dos arquivos e retornar a menor Pessoa
        Extractor extrator = new Extractor(); 
        int blocoGerado = 0; //variavel de controle para verificar se eu intercalei todos os blocos em um bloco 
        int arquivoMarcado =0; //variavel para marcar o arquivo de onde eu li o menor dado
        int arqSaidaAtual =0; //variavel para controlar em qual arquivo eu estou escrevendo

        //EQTO O BLOCO CRIADO FOR MENOR QUE O ARQUIVO ORIGINAL (NÃO ACABOU)
        while(blocoGerado < QTDPESSOAS){
            //VERIFICAR ALTERNÂNCIA DE FONTES (SE TODAS AS ATUAIS CHEGARAM AO FIM)
            // ou seja, se eu já li de todos arquivos de leitura
            if(conjuntoBlockSet.fimDeLeitura()){
                conjuntoBlockSet.apagarArqLeitura();
                conjuntoBlockSet.alternarFontes();
                conjuntoBlockSet.abrirArquivos();
                arqSaidaAtual = 0;
            }else{ //se eu não li eu abro o arquivo de saída atual
                conjuntoBlockSet.abrirArquivoEscrita(arqSaidaAtual);
            }
            //ZERAR O TAMANHO DO BLOCO ATUAL 
            blocoGerado = 0;
            //LER 1 DADO DE CADA FONTE PARA A MEMÓRIA
            extrator.extrairDeTodos(conjuntoBlockSet.getArquivoLeitura());
            //EQTO HOUVER FONTE DE DADOS DISPONÍVEL (NÃO LEU SEPARADOR)
            while(extrator.existeFonteValida()){
                qtdPassadasNoArquivo++;
                //DESCOBRIR O MENOR DADO NA MEMÓRIA
                Map.Entry<Pessoa,Integer> menor = extrator.retornaMenor();
                //GUARDAR DE ONDE VEIO ESTE DADO
                arquivoMarcado = menor.getValue();
                //ESCREVER O MENOR NO ARQUIVO DE SAÍDA ATUAL  
                menor.getKey().saveToFile(conjuntoBlockSet.getArquivoEscrita()[arqSaidaAtual]);
                //INCREMENTAR O TAMANHO DO BLOCO ATUAL 
                blocoGerado++;
                //LER O PRÓXIMO DADO DO ARQUIVO "MARCADO"
                extrator.extrairDeUm(conjuntoBlockSet.getArquivoLeitura()[arquivoMarcado], arquivoMarcado);
            }
            //ESCREVER UM SEPARADOR NO ARQUIVO DE SAÍDA ATUAL
            Pessoa.separador().saveToFile(conjuntoBlockSet.getArquivoEscrita()[arqSaidaAtual]);
            //ATUALIZAR PARA O PRÓXIMO ARQUIVO DE SAÍDA
            conjuntoBlockSet.fecharArquivoEscrita(arqSaidaAtual);
            arqSaidaAtual++;
            arqSaidaAtual %= QTARQ;
        }
        //se intercalou todos os dados eu fecho todos arquivos 
        conjuntoBlockSet.fecharTodosArquivosEscrita(); 
        conjuntoBlockSet.fecharTodosArquivosDeLeitura();
        //renomeio o arquivo onde eu ordenei para Pessoas_Ordenado.bin
        //e excluo todos os outros arquivos temporarios que foram usados para intercalação
        finalizarPrograma(arqSaidaAtual-1);

    }

    

    public static void finalizarPrograma(int arquivoOndeOrdenou){
        File ordenado = new File("arquivo_0"+arquivoOndeOrdenou+".tmp");
        File rename = new File("Pessoas_Ordenado.bin");
        //mando deletar para caso tenha sido feita alguma ordenação antes, e já exista um arquivo ordenado na pasta
        rename.delete(); 
        ordenado.renameTo(rename);
        File dFile = new File("arquivo_0"+QTARQ+".tmp");
        dFile.delete();
        for(int i =1; i<QTARQ;i++){
            dFile = new File("arquivo_0"+i+".tmp");
            dFile.delete();
            dFile = new File("arquivo_0"+(i+QTARQ)+".tmp");
            dFile.delete();
        }

    }
    
    public static int menu(Scanner leitor){
        System.out.println();
        System.out.println("\tPrograma - Intercalação Balanceada");
        System.out.println("\n0. Fim do programa");
        System.out.println("1. Intercalação com blocos FIXOS");
        System.out.println("2. Teste tempo medio execução de blocos FIXOS [ EXECUTA A INTERCALAÇÃO 10 VEZES ]");
        System.out.println("3. Intercalação com blocos VARIAVEIS");
        System.out.println("\nOpcao:");
        int opcao = Integer.parseInt(leitor.nextLine());
        return opcao;
    }

    public static void tempoMedio() throws IOException {
        long timeStart;
        long [] timefinal = new long[10];
        Scanner teclado = new Scanner(System.in);

        for(int i=0;i<10;i++){
            timeStart = System.currentTimeMillis();
            gerarBlocosFixos();
            intercalarBlocos();
            timefinal[i] = System.currentTimeMillis() - timeStart;
        }
        long media =0;
        for(int i=1;i<9;i++){
            media+=timefinal[i];
        }

        media /= 8;

        System.out.println("O tempo medio de execução, encontrado executando 10 vezes foi de "+(media/1000)/60);
        teclado.nextLine();
    }
    
    public static void main(String[] args) throws Exception {
        int opcao;
        Scanner entrada = new Scanner(System.in);
        Scanner teclado = new Scanner(System.in);
        //variaveis para medir o tempo
        long timeStart;
        long timeFinal;

        try{
            do{
                opcao = menu(entrada);
                switch(opcao){
                    case 1: 
                        System.out.println("O tempo medio de execução é de 4 minutos e meio para 5 minutos, precione <enter> para executar...");
                        teclado.nextLine();
                        System.out.println("\n\n\nBlocos sendo gerados, abra a pasta do programa e acompanhe...");
                        timeStart = System.currentTimeMillis();
                        gerarBlocosFixos();
                        intercalarBlocos();
                        timeFinal = System.currentTimeMillis() - timeStart;
                        System.out.println("\nOrdenação concluida!!\n\n");
                        System.out.println("Numero de blocos gerados: "+qtdBlocosGerados);
                        System.out.println("\nQuantidade de passadas no arquivo: "+qtdPassadasNoArquivo);
                        System.out.println("\nTempo gasto na execução do algoritmo: aproximadamente "+((timeFinal/1000)/60)+" minutos");
                        teclado.nextLine();
                        break;
                    case 2:

                        tempoMedio();
                        break;
                   
                    case 3: 
                        System.out.println("Metodo de ordenação em construção :)");
                        teclado.nextLine();
                        break;
                    default:
                        System.out.println("Adeus!!!");

                }
            }while(opcao!=0);

        }catch (IOException ex) {
            System.out.println("Ocorreu um erro: "+ex.getMessage());
        }
 
        entrada.close();
        

        
    }
}

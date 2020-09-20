package app;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Extractor {
    // Extrator para retirada do menor dado, composto por <Pessoa> uma pessoa
    // extraida
    // <Integer> arquivo no qual foi extraido

    public TreeMap<Pessoa, Integer> mapExtrator;

    public Extractor() {
        mapExtrator = new TreeMap<Pessoa, Integer>();
    }

    public void extrairDeTodos(RandomAccessFile[] arquivos){
        try{
            for(int i=0;i<arquivos.length;i++){
                if(arquivos[i].getFilePointer() < arquivos[i].length()){
                    Pessoa aux = Pessoa.readFromFile(arquivos[i]);
                    //Se eu realmente li uma pessoa eu coloco ela no extrator
                    //Se essa pessoa for um separador, eu não faço nada;
                    if(!aux.EUmSeparador()) mapExtrator.put(aux, i);
                }
            }
        }catch (IOException E){
            E.printStackTrace();
        }
    }

    public void extrairDeUm(RandomAccessFile arquivo, int nArquivo){
        try{
            if(arquivo.getFilePointer() < arquivo.length()){
                Pessoa aux = Pessoa.readFromFile(arquivo);
                //Se eu realmente li uma pessoa eu coloco ela no extrator
                //Se essa pessoa for um separador, eu não faço nada;
                if(!aux.EUmSeparador()) mapExtrator.put(aux, nArquivo);
            }
        }catch (IOException E){
            E.printStackTrace();
        }            
    }

    public boolean existeFonteValida(){
        if(mapExtrator.size() != 0) return true;

        return false;
    }

    public Entry<Pessoa,Integer> retornaMenor(){
        //O TreeMap sempre vai colocar o menor elemento na primeira posição do arranjo
        Entry <Pessoa, Integer> E = mapExtrator.entrySet().iterator().next();
        
        //se o menor elemento do arrannjo não é um separador, comparação só pra garantir
        if(!E.getKey().EUmSeparador()){
            //removo o elemento do arranjo de comparações e o retorno 
            mapExtrator.remove(E.getKey());
            return E;
        }
        
        return E;     

    }
}

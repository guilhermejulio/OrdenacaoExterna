package app;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Pessoa implements Comparable{
    protected int RG;
    protected String nome;
    protected String dataNasc;

    public Pessoa(int rg, String nome, String nasc){
        this.RG = rg;
        this.nome = nome;
        this.dataNasc = nasc;
    }


    public static Pessoa separador(){
        return new Pessoa(-1, "","");
    }

    public boolean saveToFile(RandomAccessFile file) throws IOException{
        file.seek(file.length());
        file.writeInt(this.RG);
        file.writeUTF(this.nome);
        file.writeUTF(this.dataNasc);
        return true;
    }

    public static Pessoa readFromFile(RandomAccessFile dados) throws IOException{
        Pessoa nova = Pessoa.separador();

        int rg = dados.readInt();
        String nome = dados.readUTF();
        String nasc = dados.readUTF();
        nova = new Pessoa(rg,nome,nasc);

        return nova;
    }

    @Override
    public int compareTo(Object o) { //-1 se for menor; 1 se for maior ou 0 em empate
        Pessoa outra = (Pessoa)o;

        //verifica primeiro se a comparação esta sendo feita com um separador;
        //pois sem essa verificação, o compareTo retorna que o separador é menor que uma pessoa.
          
       if(this.RG != -1 && outra.RG == -1) return -1;
       else if(this.RG == -1 && outra.RG != -1) return 1;
        if(this.nome.compareTo(outra.nome) <0) return -1;
        else if(this.nome.compareTo(outra.nome) >0) return 1;

        return 0;
    }

    public String toString(){
        return "Nome: "+nome+" RG: "+RG+" Data de nascimento: "+dataNasc;
    }

    public boolean EUmSeparador(){
        if(this.RG == -1){
            return true;
        }
        
        return false;
    }
}

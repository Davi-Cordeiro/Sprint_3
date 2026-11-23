package br.com.clinica.controller;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Criptografia de senha (PBKDF2, já vem no Java).
 * A senha nunca é salva como texto: no banco fica "sal:resultado".
 * O "sal" é um valor aleatório diferente para cada pessoa, assim duas senhas iguais
 * ficam diferentes no banco.
 */
public class Senha {

    private static final int REPETICOES = 100000;

    public static String criptografar(String senha) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal) + ":" + calcular(senha, sal);
    }

    /** A senha digitada é a mesma que foi salva? */
    public static boolean confere(String senhaDigitada, String senhaSalva) {
        if (senhaSalva == null || !senhaSalva.contains(":")) {
            return false;
        }
        String[] partes = senhaSalva.split(":");
        byte[] sal = Base64.getDecoder().decode(partes[0]);
        String resultado = calcular(senhaDigitada, sal);
        return MessageDigest.isEqual(resultado.getBytes(), partes[1].getBytes());
    }

    private static String calcular(String senha, byte[] sal) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, REPETICOES, 256);
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] resultado = fabrica.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar a senha", e);
        }
    }
}

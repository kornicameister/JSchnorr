package org.kornicameister.crypto.schnorr;

import org.kornicameister.crypto.sqlite.SQLiteController;
import org.kornicameister.crypto.sqlite.annotations.Column;
import org.kornicameister.crypto.sqlite.annotations.Id;
import org.kornicameister.crypto.sqlite.annotations.Table;
import org.kornicameister.crypto.sqlite.enums.ColumnType;

import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Class represents data model that is to be persisted
 * to database...Keys are huge numbers so it would
 * be enough to persist them in some sort of binary file,
 * but Mongo...I wanted to try it ;-)
 *
 * @author kornicameister
 * @since 0.0.1
 */

@Table(name = "schnorr3")
public class SchnorrCryptoKey {

    @Id(column = @Column(name = "id", type = ColumnType.INTEGER))
    protected Integer primaryKey;

    @Column(name = "pub_key", type = ColumnType.BIG_INTEGER)
    private BigInteger publicKey;

    @Column(name = "priv_key", type = ColumnType.BIG_INTEGER)
    private BigInteger privateKey;

    @Column(name = "e_factor", type = ColumnType.BIG_INTEGER)
    private BigInteger cryptoEPart;

    @Column(name = "y_factor", type = ColumnType.BIG_INTEGER)
    private BigInteger cryptoYPart;

    public SchnorrCryptoKey() {

    }

    public Integer getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Integer primaryKey) {
        this.primaryKey = primaryKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(BigInteger publicKey) {
        this.publicKey = publicKey;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(BigInteger privateKey) {
        this.privateKey = privateKey;
    }

    public BigInteger getCryptoEPart() {
        return cryptoEPart;
    }

    public void setCryptoEPart(BigInteger cryptoEPart) {
        this.cryptoEPart = cryptoEPart;
    }

    public BigInteger getCryptoYPart() {
        return cryptoYPart;
    }

    public void setCryptoYPart(BigInteger cryptoYPart) {
        this.cryptoYPart = cryptoYPart;
    }

    public static Integer addSchnorrKey(SchnorrCryptoKey data,
                                        SQLiteController controller) throws SQLException {
        return controller.saveObject(data);
    }

    public static SchnorrCryptoKey getSchnorrKey(Integer id, SQLiteController controller) throws SQLException {
        return (SchnorrCryptoKey) controller.getObject(id, SchnorrCryptoKey.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SchnorrCryptoKey)) return false;

        SchnorrCryptoKey cryptoKey = (SchnorrCryptoKey) o;

        return cryptoEPart.equals(cryptoKey.cryptoEPart)
                && cryptoYPart.equals(cryptoKey.cryptoYPart)
                && !(primaryKey != null ? !primaryKey.equals(cryptoKey.primaryKey) : cryptoKey.primaryKey != null)
                && privateKey.equals(cryptoKey.privateKey)
                && publicKey.equals(cryptoKey.publicKey);
    }

    @Override
    public int hashCode() {
        int result = primaryKey != null ? primaryKey.hashCode() : 0;
        result = 31 * result + publicKey.hashCode();
        result = 31 * result + privateKey.hashCode();
        result = 31 * result + cryptoEPart.hashCode();
        result = 31 * result + cryptoYPart.hashCode();
        return result;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SchnorrCryptoKey{");
        sb.append("publicKey=").append(publicKey);
        sb.append(", privateKey=").append(privateKey);
        sb.append(", primaryKey=").append(primaryKey);
        sb.append(", cryptoYPart=").append(cryptoYPart);
        sb.append(", cryptoEPart=").append(cryptoEPart);
        sb.append('}');
        return sb.toString();
    }
}

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
    protected Integer modelId;

    @Column(name = "pub_key", type = ColumnType.BIG_INTEGER)
    private BigInteger publicKey;

    @Column(name = "priv_key", type = ColumnType.BIG_INTEGER)
    private BigInteger privateKey;

    @Column(name = "e_factor", type = ColumnType.BIG_INTEGER)
    private BigInteger factorE;

    @Column(name = "y_factor", type = ColumnType.BIG_INTEGER)
    private BigInteger factorY;

    public SchnorrCryptoKey() {

    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
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

    public BigInteger getFactorE() {
        return factorE;
    }

    public void setFactorE(BigInteger factorE) {
        this.factorE = factorE;
    }

    public void setFactorY(BigInteger factorX) {
        this.factorY = factorX;
    }

    public BigInteger getFactorY() {
        return factorY;
    }

    public static Integer addSchnorrKey(SchnorrCryptoKey data,
                                        SQLiteController controller) throws SQLException {
        return controller.saveObject(data);
    }

    public static SchnorrCryptoKey getSchnorrKey(Integer id, SQLiteController controller) throws SQLException {
        return (SchnorrCryptoKey) controller.getObject(id, SchnorrCryptoKey.class);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SchnorrCryptoKey{");
        sb.append("publicKey=").append(publicKey);
        sb.append(", privateKey=").append(privateKey);
        sb.append(", factorE=").append(factorE);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (!(o instanceof SchnorrCryptoKey)) return false;

        SchnorrCryptoKey cryptoKey = (SchnorrCryptoKey) o;

        return !(factorE != null ? !factorE.equals(cryptoKey.factorE) : cryptoKey.factorE != null)
                && !(factorY != null ? !factorY.equals(cryptoKey.factorY) : cryptoKey.factorY != null)
                && !(modelId != null ? !modelId.equals(cryptoKey.modelId) : cryptoKey.modelId != null)
                && !(privateKey != null ? !privateKey.equals(cryptoKey.privateKey) : cryptoKey.privateKey != null)
                && !(publicKey != null ? !publicKey.equals(cryptoKey.publicKey) : cryptoKey.publicKey != null);
    }

    @Override
    public int hashCode() {
        int result = modelId != null ? modelId.hashCode() : 0;
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        result = 31 * result + (privateKey != null ? privateKey.hashCode() : 0);
        result = 31 * result + (factorE != null ? factorE.hashCode() : 0);
        result = 31 * result + (factorY != null ? factorY.hashCode() : 0);
        return result;
    }
}

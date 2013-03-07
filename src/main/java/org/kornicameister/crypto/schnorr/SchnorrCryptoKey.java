package org.kornicameister.crypto.schnorr;

import org.kornicameister.crypto.sqlite.SQLiteController;
import org.kornicameister.crypto.sqlite.annotations.Column;
import org.kornicameister.crypto.sqlite.annotations.Id;
import org.kornicameister.crypto.sqlite.annotations.Table;

import java.math.BigInteger;

/**
 * Class represents data model that is to be persisted
 * to database...Keys are huge numbers so it would
 * be enough to persist them in some sort of binary file,
 * but Mongo...I wanted to try it ;-)
 *
 * @author kornicameister
 * @since 0.0.1
 */

@Table(name = "keys2")
public class SchnorrCryptoKey {
    protected SQLiteController controller;

    @Id
    @Column(name = "id", type = Column.Types.INTEGER)
    protected Integer modelId;

    @Column(name = "pub_key", type = Column.Types.BLOB)
    private BigInteger publicKey;

    @Column(name = "priv_key", type = Column.Types.BLOB)
    private BigInteger privateKey;

    private BigInteger factorE;

    public SchnorrCryptoKey() {

    }

    public SchnorrCryptoKey(SQLiteController controller) {
        this.controller = controller;
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

    public Integer addSchnorrKey(SchnorrCryptoKey data) throws Exception {
        return this.controller.add(data);
    }

    public SchnorrCryptoKey getSchnorrKey(Integer id) {
        return (SchnorrCryptoKey) this.controller.get(id, this.getClass());
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
}

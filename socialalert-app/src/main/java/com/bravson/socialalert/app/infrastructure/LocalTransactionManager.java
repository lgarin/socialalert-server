package com.bravson.socialalert.app.infrastructure;

import javax.transaction.Status;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class LocalTransactionManager extends AbstractPlatformTransactionManager {

	private static final long serialVersionUID = 1L;
	
	private final ThreadLocal<Integer> localTx = new ThreadLocal<Integer>() {
		protected Integer initialValue() {
			return Status.STATUS_NO_TRANSACTION;
		}
	};
	
	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition)
			throws TransactionException {
		localTx.set(Status.STATUS_ACTIVE);
	}
	
	@Override
	protected void doCommit(DefaultTransactionStatus status)
			throws TransactionException {
		localTx.set(Status.STATUS_COMMITTED);
	}
	
	@Override
	protected Object doGetTransaction() throws TransactionException {
		return localTx;
	}
	
	@Override
	protected void doRollback(DefaultTransactionStatus status)
			throws TransactionException {
		localTx.set(Status.STATUS_ROLLEDBACK);
	}
	
	@Override
	protected boolean isExistingTransaction(Object transaction)
			throws TransactionException {
		int status = localTx.get();
		return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
	}
	
	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status)
			throws TransactionException {
		localTx.set(Status.STATUS_MARKED_ROLLBACK);
	}
	
	@Override
	protected boolean useSavepointForNestedTransaction() {
		return false;
	}
}

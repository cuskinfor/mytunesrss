package de.codewave.utils.sql;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EmptyQueryResult<E> implements QueryResult<E> {

    @Override
    public int getResultSize() {
        return 0;
    }

    @Override
    public List<E> getResults() {
        return Collections.<E>emptyList();
    }

    @Override
    public List<E> getResults(boolean keepResultSetOpen) {
        return Collections.<E>emptyList();
    }

    @Override
    public List<E> getRemainingResults() {
        return Collections.<E>emptyList();
    }

    @Override
    public List<E> getRemainingResults(boolean keepResultSetOpen) {
        return Collections.<E>emptyList();
    }

    @Override
    public List<E> getNextResults(int count) {
        return Collections.<E>emptyList();
    }

    @Override
    public List<E> getNextResults(int count, boolean keepResultSetOpen) {
        return Collections.<E>emptyList();
    }

    @Override
    public List<E> getResults(int start, int count) throws SQLException {
        return Collections.<E>emptyList();
    }

    @Override
    public List<E> getResults(int start, int count, boolean keepResultSetOpen) throws SQLException {
        return Collections.<E>emptyList();
    }

    @Override
    public void addResults(Collection<E> target) {
        // nothing to do
    }

    @Override
    public void addResults(Collection<E> target, boolean keepResultSetOpen) {
        // nothing to do
    }

    @Override
    public void addRemainingResults(Collection<E> target) {
        // nothing to do
    }

    @Override
    public void addRemainingResults(Collection<E> target, boolean keepResultSetOpen) {
        // nothing to do
    }

    @Override
    public void addNextResults(Collection<E> target, int count) {
        // nothing to do
    }

    @Override
    public void addNextResults(Collection<E> target, int count, boolean keepResultSetOpen) {
        // nothing to do
    }

    @Override
    public void addResults(Collection<E> target, int start, int count) throws SQLException {
        // nothing to do
    }

    @Override
    public void addResults(Collection<E> target, int start, int count, boolean keepResultSetOpen) throws SQLException {
        // nothing to do
    }

    @Override
    public void processResults(DataStoreQuery.ResultProcessor<E> processor) {
        // nothing to do
    }

    @Override
    public void processResults(DataStoreQuery.ResultProcessor<E> processor, boolean keepResultSetOpen) {
        // nothing to do
    }

    @Override
    public void processRemainingResults(DataStoreQuery.ResultProcessor<E> processor) {
        // nothing to do
    }

    @Override
    public void processRemainingResults(DataStoreQuery.ResultProcessor<E> processor, boolean keepResultSetOpen) {
        // nothing to do
    }

    @Override
    public void processNextResults(DataStoreQuery.ResultProcessor<E> processor, int count) {
        // nothing to do
    }

    @Override
    public void processNextResults(DataStoreQuery.ResultProcessor<E> processor, int count, boolean keepResultSetOpen) {
        // nothing to do
    }

    @Override
    public void processResults(DataStoreQuery.ResultProcessor<E> processor, int start, int count) throws SQLException {
        // nothing to do
    }

    @Override
    public void processResults(DataStoreQuery.ResultProcessor<E> processor, int start, int count, boolean keepResultSetOpen) throws SQLException {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public void reset() {
        // nothing to do
    }

    @Override
    public E nextResult() {
        return null;
    }

    @Override
    public E getResult(int index) {
        return null;
    }
}

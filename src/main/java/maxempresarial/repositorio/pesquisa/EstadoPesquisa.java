package maxempresarial.repositorio.pesquisa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import maxempresarial.modelo.Estado;
import maxempresarial.repositorio.filtro.EstadoFiltro;

public class EstadoPesquisa implements Serializable {

	private static final long serialVersionUID = 1L;

	private final EntityManager gerenciadorEntidade;

	@Inject
	public EstadoPesquisa(EntityManager gerenciadorEntidade) {
		super();
		this.gerenciadorEntidade = gerenciadorEntidade;
	}

	public List<Estado> filtrados(EstadoFiltro filtro) {
		From<?, ?> orderByFromEntity = null;

		CriteriaBuilder builder = this.gerenciadorEntidade.getCriteriaBuilder();
		CriteriaQuery<Estado> criteriaQuery = builder.createQuery(Estado.class);

		Root<Estado> estadoRoot = criteriaQuery.from(Estado.class);
		List<Predicate> predicates = criarPredicatesParaFiltro(filtro, estadoRoot);
		criteriaQuery.where(predicates.toArray(new Predicate[0]));

		if (filtro.getPropriedadeParaOrdenacao() != null) {
			String nomePropriedadeOrdenacao = filtro.getPropriedadeParaOrdenacao();
			orderByFromEntity = estadoRoot;

			if (filtro.getPropriedadeParaOrdenacao().contains(".")) {
				nomePropriedadeOrdenacao = nomePropriedadeOrdenacao.substring(filtro.getPropriedadeParaOrdenacao().indexOf(".") + 1);
			}

			if (filtro.isAscendente() && filtro.getPropriedadeParaOrdenacao() != null) {
				criteriaQuery.orderBy(builder.asc(orderByFromEntity.get(nomePropriedadeOrdenacao)));
			} else if (filtro.getPropriedadeParaOrdenacao() != null) {
				criteriaQuery.orderBy(builder.desc(orderByFromEntity.get(nomePropriedadeOrdenacao)));
			}
		}

		TypedQuery<Estado> query = this.gerenciadorEntidade.createQuery(criteriaQuery);
		query.setFirstResult(filtro.getPrimeiroRegistro());
		query.setMaxResults(filtro.getQuantidadeDeRegistros());

		return query.getResultList();
	}

	public int quantidadeEstadosFiltrados(EstadoFiltro filtro) {

		CriteriaBuilder builder = this.gerenciadorEntidade.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);

		Root<Estado> estadoRoot = criteriaQuery.from(Estado.class);
		List<Predicate> predicates = criarPredicatesParaFiltro(filtro, estadoRoot);
		criteriaQuery.select(builder.count(estadoRoot));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));

		TypedQuery<Long> query = this.gerenciadorEntidade.createQuery(criteriaQuery);
		return query.getSingleResult().intValue();
	}

	private List<Predicate> criarPredicatesParaFiltro(EstadoFiltro filtro, Root<Estado> estadoRoot) {
		CriteriaBuilder builder = this.gerenciadorEntidade.getCriteriaBuilder();
		List<Predicate> predicates = new ArrayList<>();

		if (StringUtils.isNotBlank(filtro.getNome())) {
			predicates.add(builder.like(estadoRoot.<String>get("nome"), "%" + filtro.getNome() + "%"));
		}

		return predicates;
	}

}

package me.dio.credit.application.system.service.impl

import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.ICreditService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.*

@Service
class CreditService(
  private val creditRepository: CreditRepository,
  private val customerService: CustomerService
) : ICreditService {

  // Método responsável por salvar uma instância de Credit
  override fun save(credit: Credit): Credit {
    this.validDayFirstInstallment(credit.dayFirstInstallment) // Valida a data do primeiro vencimento
    credit.apply {
      customer = customerService.findById(credit.customer?.id!!) // Obtém o cliente associado ao crédito
    }
    return this.creditRepository.save(credit) // Salva a instância de Credit no repositório
  }

  // Método responsável por encontrar todos os créditos associados a um cliente
  override fun findAllByCustomer(customerId: Long): List<Credit> =
    this.creditRepository.findAllByCustomerId(customerId) // Retorna os créditos encontrados no repositório

  // Método responsável por encontrar um crédito pelo código e verificar se pertence ao cliente especificado
  override fun findByCreditCode(customerId: Long, creditCode: UUID): Credit {
    val credit: Credit = (this.creditRepository.findByCreditCode(creditCode)
      ?: throw BusinessException("Creditcode $creditCode not found")) // Busca o crédito pelo código no repositório, ou lança exceção se não encontrado

    return if (credit.customer?.id == customerId) credit // Verifica se o cliente associado ao crédito é o mesmo especificado. Se for, retorna o crédito.
    else throw IllegalArgumentException("Contact admin") // Caso contrário, lança uma exceção de argumento inválido.
  }

  // Método privado que valida a data do primeiro vencimento
  private fun validDayFirstInstallment(dayFirstInstallment: LocalDate): Boolean {
    return if (dayFirstInstallment.isBefore(LocalDate.now().plusMonths(3))) true // Verifica se a data do primeiro vencimento é anterior a três meses a partir da data atual.
    else throw BusinessException("Invalid Date") // Caso contrário, lança uma exceção de negócio indicando uma data inválida.
  }
}


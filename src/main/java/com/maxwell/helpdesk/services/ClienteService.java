package com.maxwell.helpdesk.services;

import com.maxwell.helpdesk.domain.Cliente;
import com.maxwell.helpdesk.domain.Pessoa;
import com.maxwell.helpdesk.domain.dtos.ClienteDTO;
import com.maxwell.helpdesk.repositories.ClienteRepository;
import com.maxwell.helpdesk.repositories.PessoaRepository;
import com.maxwell.helpdesk.services.exceptions.DataIntegrityViolationException;
import com.maxwell.helpdesk.services.exceptions.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final PessoaRepository pessoaRepository;

    public ClienteService(ClienteRepository clienteRepository, PessoaRepository pessoaRepository) {
        this.clienteRepository = clienteRepository;
        this.pessoaRepository = pessoaRepository;
    }

    public Cliente findById(Integer id){
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado com o id: "+id));
    }

    public List<ClienteDTO> findAll() {
        return clienteRepository.findAll().stream()
                .map(ClienteDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO save(ClienteDTO objDTO) {
        objDTO.setId(null);
        validaPorCpfEEmail(objDTO);
        Cliente cliente = new Cliente(objDTO);
        clienteRepository.save(cliente);
        return new ClienteDTO(cliente);
    }

    @Transactional
    public ClienteDTO update(Integer id, ClienteDTO objDTO) {
        objDTO.setId(id);
        Cliente oldObj = findById(id);
        validaPorCpfEEmail(objDTO);
        oldObj = new Cliente(objDTO);
        clienteRepository.save(oldObj);
        return new ClienteDTO(oldObj);
    }

    public void delete(Integer id) {
        Cliente obj = findById(id);
        if(obj.getChamados().size() > 0){
            throw new DataIntegrityViolationException("Cliente possui ordens de serviço e não pode ser deletado.");
        }
        clienteRepository.delete(obj);
    }

    private void validaPorCpfEEmail(ClienteDTO objDTO) {
        Optional<Pessoa> obj = pessoaRepository.findByCpf(objDTO.getCpf());
        if(obj.isPresent() && !Objects.equals(obj.get().getId(), objDTO.getId())){
            throw new DataIntegrityViolationException("Já existe um usuário com o cpf: "+objDTO.getCpf());
        }
        obj = pessoaRepository.findByEmail(objDTO.getEmail());
        if(obj.isPresent() && !Objects.equals(obj.get().getId(), objDTO.getId())){
            throw new DataIntegrityViolationException("Já existe um usuário com o email: "+objDTO.getEmail());
        }
    }
}

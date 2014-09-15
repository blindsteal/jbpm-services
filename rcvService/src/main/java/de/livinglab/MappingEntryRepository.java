package de.livinglab;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingEntryRepository extends
		CrudRepository<MappingEntry, Long> {

	List<MappingEntry> findByRemoteDef2AndRemoteId2(String remoteDef2, int remoteId2);
	List<MappingEntry> findByLocalDef2AndLocalId2(String localDef2, int localId2);
}

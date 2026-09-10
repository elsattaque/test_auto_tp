import { describe, expect, vi, it } from 'vitest';
import { todo } from '../todo';
import {createGuildKeeperClient} from "../../src";
import {fetchedUrl, jsonResponse} from "./httpTestSupport.ts";
import { NotFoundError } from "../../src";

// Chapitre 3 — « Tester une fonction asynchrone » (TP).
//
// Modèle : tests/client/guildKeeperClient.test.ts (fetchImpl simulé + async/await).
// Cible : client.members.assignments(name) et client.members.get(name).
// Une fois écrit, remplacer `todo(` par `it(`.

const BASE = 'http://api.test';

function clientWith(response: Response) {
  const fetchImpl = vi.fn().mockResolvedValue(response);
  return { client: createGuildKeeperClient({ baseUrl: BASE, fetchImpl }), fetchImpl };
}

describe('members (asynchrone) — TP chapitre 3', () => {
  // fournir un fetchImpl qui répond 200 avec une liste d'attributions,
  // puis `await client.members.assignments('Dragan')` et vérifier le contenu.
  it('members.assignements(name) résout la liste des attributios ', async() => {
    // arrange
    const assignments = [
      {questId: '1', questTitle: 'Nettoyer les caves de la guilde', status: 'COMPLETED'},
      {questId: '2', questTitle: 'Escorter la caravane marchande', status: 'ASSIGNED' }
    ]
    const { client, fetchImpl } = clientWith(jsonResponse(assignments))

    // act
    const result = await client.members.assignments('Dragan');

    // assert
    expect(fetchedUrl(fetchImpl)).toBe('http://api.test/api/v1/members/Dragan/assignments');
    expect(result).toHaveLength(2);
    expect(assignments.map((a) => a.status)).toEqual(['COMPLETED', 'ASSIGNED']);
  });

  // fournir un fetchImpl qui répond 404 { "error": "NOT_FOUND" },
  // puis `await expect(client.members.get('Gandalf')).rejects.toThrow(NotFoundError)`.
  it('members.get(name) rejette avec NotFoundError pour un membre inconnu', async() => {
    // arrange
    const { client } = clientWith(jsonResponse({ error: 'NOT_FOUND' }, {status: 404}));

    // act & assert
    await expect(client.members.get('Gandalf')).rejects.toThrow(NotFoundError);
  });
});

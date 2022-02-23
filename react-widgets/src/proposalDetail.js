import React from 'react';
import axios from 'axios';
import EventCard from './EventICard';
import { Card, Header, Divider, Confirm, Message, Container, Button, Segment, Label, Grid, Icon, Form, Dropdown } from 'semantic-ui-react';

class ProposalDetail extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      uid: props.uid !== '' ? parseInt(props.uid) : null,
      proposal: props.proposal ? JSON.parse(props.proposal) : null,
      error: props.error,
      tmEvents: [],
      draftVotes: [],
      updatingPreferences: false,
      eventToDelete: null,
      eventBeingDeleted: null,
      aboutToDeleteProposal: false,
      deletingProposal: false,
      proposalDeleted: false,
      updatingAccept: null,
      userIdToDelete: null,
      deletingUser: false,
      aboutToRejectProposal: false,
      updatingReject: false,
      finalizingProposal: false
    };
  }

  handlePreferenceChange = (eventId, canAttend, rating) => {
    this.setState(({ draftVotes }) => ({
      draftVotes: [
        ...draftVotes.filter(vote => vote.eventId != eventId),
        { eventId, canAttend, rating }
      ]
    }));
  }

  handleUpdatePreferences = (saveDraft) => {

    let { proposal, draftVotes, uid } = this.state;
    let votes = [];

    // combine local draft with draft saved earlier
    for (let event of proposal.events) {
      let draftVote = draftVotes.find(vote => vote.eventId === event.id) // if have a local draft
        ?? event.votes.find(vote => vote.user.id === uid); // or draft from the backend
      if (draftVote !== undefined) {
        votes.push({ eventId: event.id, canAttend: draftVote.canAttend, rating: draftVote.rating });
      }
    }

    if (!saveDraft) { // if committing preferences, make sure it is complete
      if (votes.length != proposal.events.length
        || votes.find(vote => vote.canAttend === null || vote.rating === null) !== undefined) {
        this.setState({ error: 'Please fill out preferences for all events.' });
        return;
      }
    }

    let body = { proposalId: this.state.proposal?.id, votes, isDraft: saveDraft };
    this.setState({ updatingPreferences: saveDraft ? 'draft' : 'commit' });
    axios.post('/api/event-vote', body)
      .then(response => {
        this.setState({
          proposal: response.data,
          error: null,
          updatingPreferences: false,
          draftVotes: []
        });
      })
      .catch(error => {
        console.log(error);
        this.setState({
          error: String(error),
          updatingPreferences: false
        });
      });
  }

  handleShowDeleteEventConfirm = (eventId) => {
    this.setState({ eventToDelete: eventId });
  }

  handleHideDeleteEventConfirm = () => {
    this.setState({ eventToDelete: null });
  }

  handleDeleteEvent = () => {
    let { eventToDelete, proposal } = this.state;
    if (proposal.events.length <= 1) {
      this.handleDeleteProposal();
    } else {
      let body = { proposalId: this.state.proposal.id, eventId: eventToDelete };
      this.setState({ eventBeingDeleted: eventToDelete, eventToDelete: null });
      axios.post('/api/delete-event', body)
        .then(response => {
          this.setState({
            proposal: response.data,
            error: null,
            eventBeingDeleted: null
          });
        })
        .catch(error => {
          console.log(error);
          this.setState({
            error: String(error),
            eventBeingDeleted: null
          });
        });
    }
  }

  handleShowDeleteProposalConfirm = () => {
    this.setState({ aboutToDeleteProposal: true });
  }

  handleHideDeleteProposalConfirm = () => {
    this.setState({ aboutToDeleteProposal: false });
  }

  handleDeleteProposal = () => {
    let body = { proposalId: this.state.proposal?.id };
    this.setState({ deletingProposal: true });
    axios.post('/api/delete-proposal', body)
      .then(() => {
        this.setState({
          proposal: null,
          events: null,
          proposalDeleted: true,
          error: null,
          deletingProposal: false,
          aboutToDeleteProposal: false
        });
      })
      .catch(error => {
        console.log(error);
        this.setState({
          error: String(error),
          deletingProposal: null,
          aboutToDeleteProposal: false
        });
      });
  }

  handleAcceptProposal = (accept) => {
    let body = { proposalId: this.state.proposal?.id, accept };
    this.setState({ updatingAccept: accept });
    axios.post('/api/accept-proposal', body)
      .then(response => {
        this.setState({
          proposal: response.data,
          error: null,
          updatingAccept: null
        });
      })
      .catch(error => {
        console.log(error);
        this.setState({
          error: String(error),
          updatingAccept: null
        });
      });
  }

  handleShowDeleteUserConfirm = (id) => {
    this.setState({ userIdToDelete: id });
  }

  handleHideDeleteUserConfirm = () => {
    this.setState({ userIdToDelete: null });
  }

  handleDeleteUser = () => {
    let { proposal, userIdToDelete } = this.state;
    if (proposal.invitedUsers.length <= 1) {
      this.setState({ userIdToDelete: null });
      this.handleDeleteProposal();
    } else {
      let body = { proposalId: proposal?.id, invitedUserId: userIdToDelete };
      this.setState({ deletingUser: true, userIdToDelete: null });
      axios.post('/api/delete-invited-user', body)
        .then(response => {
          this.setState({
            proposal: response.data,
            error: null,
            deletingUser: null
          });
        })
        .catch(error => {
          console.log(error);
          this.setState({
            error: String(error),
            deletingUser: null
          });
        });
    }
  }

  handleShowRejectConfirm = () => {
    this.setState({ aboutToRejectProposal: true });
  }

  handleHideRejectConfirm = () => {
    this.setState({ aboutToRejectProposal: false });
  }

  handleRejectProposal = () => {
    let { proposal } = this.state;
    let body = { proposalId: proposal.id, hide: true };
    this.setState({ updatingReject: true, aboutToRejectProposal: false });
    axios.post('/api/hide-proposal', body)
      .then(response => {
        this.setState({
          proposal: response.data,
          error: null,
          updatingReject: false
        });
      })
      .catch(error => {
        console.log(error);
        this.setState({
          error: String(error),
          updatingReject: false
        });
      });
  }

  handleFinalizeProposal = () => {
    let { proposal } = this.state;
    let body = { proposalId: proposal.id };
    this.setState({ finalizingProposal: true });
    axios.post('/api/finalize-proposal', body)
      .then(response => {
        this.setState({
          proposal: response.data,
          error: null,
          finalizingProposal: false
        });
      })
      .catch(error => {
        console.log(error);
        this.setState({
          error: String(error),
          finalizingProposal: false
        });
      });
  }

  componentDidMount() {
    let { proposal } = this.state;
    if (proposal) {
      let ids = (proposal?.events ?? []).map(event => event?.tmEventKey);
      axios.get('/api/tm-api-proxy', {
        params: {
          id: ids.join(','),
          size: ids.length
        }
      })
        .then(response => {
          console.log(response);
          this.setState({
            tmEvents: response.data?._embedded?.events ?? [],
          });
        })
        .catch(error => {
          console.log(error);
          this.setState({
            error: String(error)
          });
        });
    }
  }

  renderSummary = () => {

    let { uid,
      proposal,
      deletingProposal,
      deletingUser,
      userIdToDelete,
      aboutToDeleteProposal,
      aboutToRejectProposal,
      updatingReject } = this.state;

    let { invitedUsers, acceptedUsers, declinedUsers } = proposal;
    const isOwner = uid === proposal?.owner?.id;
    const finalized = proposal.finalizedEvent !== undefined;
    const hidden = proposal.hiddenUsers?.find(user => user.id === uid) !== undefined;

    return (
      <Segment vertical>
        <Header content='Summary' as='h3' />
        <Grid stackable columns='3' padded='vertically' >
          <Grid.Column>
            <Header size='small' style={{ marginBottom: '0.5rem' }}>
              {deletingUser ? <Icon name='circle notch' loading /> : <Icon name='user circle outline' />}
              Invited Users
            </Header>
            <Label.Group>
              {invitedUsers.map(user =>
                <Label
                  content={user.username}
                  key={user.id}
                  onRemove={!finalized && isOwner ? () => this.handleShowDeleteUserConfirm(user.id) : null} />)}
            </Label.Group>
          </Grid.Column>
          {finalized &&
            <Grid.Column>
              <Header size='small' style={{ marginBottom: '0.5rem' }}>
                <Icon name='smile outline' color='green' />
                Accepted Users
              </Header>
              <Label.Group>
                {acceptedUsers.map(user => <Label content={user.username} key={user.id} />)}
                {acceptedUsers.length === 0 && <Label content='None' basic />}
              </Label.Group>
            </Grid.Column>
          }
          {finalized &&
            <Grid.Column>
              <Header size='small' style={{ marginBottom: '0.5rem' }}>
                <Icon name='frown outline' color='red' />
                Declined Users
              </Header>
              <Label.Group>
                {declinedUsers.map(user => <Label content={user.username} key={user.id} />)}
                {declinedUsers.length === 0 && <Label content='None' basic />}
              </Label.Group>
            </Grid.Column>
          }
        </Grid>
        {hidden &&
          <Message
            header='Proposal Rejected'
            content='You will no longer see this proposal in your proposal list page.' />}
        {!isOwner && !finalized && !hidden &&
          <div>
            <Button
              content='Reject Proposal'
              disabled={updatingReject}
              loading={updatingReject}
              onClick={this.handleShowRejectConfirm} />
          </div>
        }
        {isOwner &&
          <div>
            <Button
              content='Delete Proposal'
              color='red'
              onClick={this.handleShowDeleteProposalConfirm}
              disabled={deletingProposal}
              loading={deletingProposal} />
          </div>
        }
        <Confirm
          open={aboutToDeleteProposal}
          header='Delete Proposal'
          content='Are you sure you want to delete this proposal?'
          onCancel={this.handleHideDeleteProposalConfirm}
          onConfirm={this.handleDeleteProposal}
        />
        <Confirm
          open={userIdToDelete !== null}
          header='Delete Invited User'
          content={
            `${invitedUsers.find(user => user.id === userIdToDelete)?.username} will be removed from the proposal.`
            + (invitedUsers.length <= 1 ? " Deleting this user will also delete the current proposal." : "")
          }
          onCancel={this.handleHideDeleteUserConfirm}
          onConfirm={this.handleDeleteUser}
        />
        <Confirm
          open={aboutToRejectProposal}
          header='Reject Proposal'
          content='Are you sure you want to reject this proposal?'
          onCancel={this.handleHideRejectConfirm}
          onConfirm={this.handleRejectProposal}
        />
      </Segment>
    );
  }

  renderEvents = () => {

    let {
      uid,
      proposal,
      tmEvents,
      updatingPreferences,
      eventToDelete: deleteEventKey,
      draftVotes
    } = this.state;

    const finalized = proposal?.finalizedEvent !== undefined;
    const committed = proposal.events?.[0]?.votes?.find(vote => vote.user.id === uid)?.isDraft === false;
    const haveLocalDraft = draftVotes.length > 0;
    const hidden = proposal.hiddenUsers?.find(user => user.id === uid) !== undefined;

    return (
      <Segment vertical>

        <Header content='Events' as='h3' />

        <Card.Group itemsPerRow={3} doubling stackable>
          {this.renderEventCards()}
        </Card.Group>

        {committed &&
          <Message
            success
            header='Response Recorded'
            content='Your preferences have been recorded. Other invited users can see you response.' />}

        <Divider hidden />
        {!finalized && !committed && !hidden &&
          <Button
            content='Commit Preferences'
            onClick={() => this.handleUpdatePreferences(false)}
            disabled={updatingPreferences === 'commit'}
            loading={updatingPreferences === 'commit'} />}
        {!finalized && !committed && !hidden &&
          <Button
            content='Save As Draft'
            onClick={() => this.handleUpdatePreferences(true)}
            disabled={!haveLocalDraft || updatingPreferences === 'draft'}
            loading={updatingPreferences === 'draft'} />}
        <Confirm
          open={deleteEventKey !== null}
          header='Delete Event'
          content={
            `${tmEvents.find(event => event?.id === deleteEventKey)?.name} will be removed from the proposal.`
            + (proposal.events.length <= 1 ? " Deleting this event will also delete the current proposal." : "")
          }
          onCancel={this.handleHideDeleteEventConfirm}
          onConfirm={this.handleDeleteEvent}
        />

      </Segment>
    );
  }

  renderEventCards = () => {

    let { proposal, tmEvents, uid, draftVotes, eventBeingDeleted } = this.state;

    return proposal.events.map(event => {
      const tmEvent = tmEvents.find(tmEvent => tmEvent.id == event.tmEventKey);
      const savedPreference = event.votes.find(vote => vote.user.id === uid);
      const myVote = draftVotes.find(vote => vote.eventId === event.id) // if we have a local draft
        ?? savedPreference // or one from the backend
        ?? { canAttend: null, rating: null, isDraft: true }; // fallback
      const isDraft = myVote.isDraft ?? true;
      const finalized = proposal.finalizedEvent !== undefined;
      const isOwner = uid === proposal.owner.id;
      const hidden = proposal.hiddenUsers?.find(user => user.id === uid) !== undefined;

      const attendOptions = [
        { key: 1, text: 'Yes', value: 'YES' },
        { key: 2, text: 'No', value: 'NO' },
        { key: 3, text: 'Maybe', value: 'MAYBE' },
      ]

      const ratingOptions = [
        { key: 1, text: "Don't Care at All", value: 1 },
        { key: 2, text: 'Not Interested', value: 2 },
        { key: 3, text: 'Meh', value: 3 },
        { key: 4, text: 'Excited', value: 4 },
        { key: 5, text: 'Very Excited', value: 5 },
      ];

      const voteForm = (<Form>
        <Form.Field>
          <label>Can Attend?</label>
          <Dropdown
            options={attendOptions}
            selection
            fluid
            placeholder='No Answer'
            value={myVote.canAttend}
            disabled={!isDraft || hidden}
            onChange={(e, { value }) => this.handlePreferenceChange(event.id, value, myVote.rating)} />
        </Form.Field>
        <Form.Field>
          <label>Excited?</label>
          <Dropdown
            options={ratingOptions}
            selection
            fluid
            placeholder='No Answer'
            value={myVote.rating}
            disabled={!isDraft || hidden}
            onChange={(e, { value }) => this.handlePreferenceChange(event.id, myVote.canAttend, value)} />
        </Form.Field>
        {isOwner && !finalized &&
          <div>
            <Divider />
            <Form.Field>
              <Button
                color='red'
                content="Delete"
                fluid
                disabled={eventBeingDeleted === event.id}
                loading={eventBeingDeleted === event.id}
                onClick={() => this.handleShowDeleteEventConfirm(event.id)} />
            </Form.Field>
          </div>}
      </Form>);

      return (<EventCard event={event}
        tmEventData={tmEvent}
        key={event?.id}
        extra={<Card.Content extra>{voteForm}</Card.Content>}
      />)
    });
  }

  renderBestEvent = () => {

    let {
      uid,
      proposal,
      tmEvents,
      updatingAccept,
      finalizingProposal
    } = this.state;

    const finalized = proposal.finalizedEvent !== undefined;
    const isOwner = uid === proposal.owner.id;
    const accepted = proposal.acceptedUsers.find(user => user.id === uid) !== undefined;
    const declined = proposal.declinedUsers.find(user => user.id === uid) !== undefined;

    if (!finalized) {
      if (isOwner) {
        return (
          <Segment vertical>
            <Header content="Best Event" as='h3' />
            <Message>
              <Message.Header>Proposal In Progress</Message.Header>
              You can finalize the proposal when ready.
            </Message>
            <Button
              content='Finalize'
              disabled={finalizingProposal}
              loading={finalizingProposal}
              onClick={this.handleFinalizeProposal} />
          </Segment>
        );
      } else { // not the owner
        return (
          <Segment vertical>
            <Header content="Best Event" as='h3' />
            <Message>
              <Message.Header>Proposal In Progress</Message.Header>
              Ask {proposal.owner.username} to finalize the proposal when ready.
            </Message>
          </Segment>
        );
      }
    } else { // finalized
      let { finalizedEvent } = proposal;
      return (
        <Segment vertical>
          <Header content="Best Event" as='h3' />
          <Card.Group itemsPerRow={3} doubling stackable>
            <EventCard
              event={finalizedEvent}
              tmEventData={tmEvents.find(tmEvent => tmEvent?.id === finalizedEvent.tmEventKey)}
            />
          </Card.Group>
          <Divider hidden />
          {!accepted &&
            <Button
              content={declined ? 'Update & Accept' : 'Accept'}
              color='green'
              loading={updatingAccept === true}
              disabled={updatingAccept !== null}
              onClick={() => this.handleAcceptProposal(true)} />
          }
          {!declined &&
            <Button
              content={accepted ? 'Update & Decline' : 'Decline'}
              color='red'
              loading={updatingAccept === false}
              disabled={updatingAccept !== null}
              onClick={() => this.handleAcceptProposal(false)} />
          }
        </Segment>
      );
    }
  }

  renderProposal = () => {

    let { proposal, error } = this.state;

    return (
      <Container>

        <Segment vertical>
          <Header as='h2' textAlign='center' color='blue'>
            {proposal.title}
            <Header.Subheader>
              Created by {proposal.owner.username}
            </Header.Subheader>
          </Header>
          {error && <Message error content={error} />}
        </Segment>

        {this.renderSummary()}

        {this.renderBestEvent()}

        {this.renderEvents()}

      </Container>
    );
  }

  renderErrorOnly = () => {
    let { error } = this.state;

    return (
      <Container>
        <Message error content={error} />
      </Container>
    );
  }

  renderDeleted = () => {
    return (
      <Container>
        <Message>
          <Message.Header>Proposal Deleted</Message.Header>
          This proposal is longer be visible to you and invited users.
        </Message>
      </Container>
    );
  }

  render() {
    if (this.state.proposalDeleted) {
      return this.renderDeleted();
    } else if (this.state.proposal === null) {
      return this.renderErrorOnly();
    }
    return this.renderProposal();
  }

}

export default ProposalDetail;